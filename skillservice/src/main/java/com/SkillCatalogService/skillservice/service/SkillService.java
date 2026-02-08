package com.SkillCatalogService.skillservice.service;

import com.SkillCatalogService.skillservice.DTO.SkillRequest;
import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.config.KafkaProperties;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.SkillDeletionException;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.SkillNotFoundException;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.UserNotFound;
import com.SkillCatalogService.skillservice.kafka.SkillEventsProducer;
import com.SkillCatalogService.skillservice.model.Skill;
import com.SkillCatalogService.skillservice.model.SkillStatus;
import com.SkillCatalogService.skillservice.openSearch.SkillSearchIndexer;
import com.SkillCatalogService.skillservice.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository repository;
    private final SkillSearchIndexer indexer;
    private final SkillEventsProducer producer;
    private final AuthClient webClient; // Keep for validation
    private final UserClient userClient; // Add for details
    private final KafkaProperties kafkaProperties;
    private final SkillSearchService searchService;

    @Value("${kafka.topic.skill-events}")
    private String skillTopic;

    // ==================== PUBLIC METHODS ====================

    public List<SkillResponse> getAllSkills(String search, String level) {
        List<SkillResponse> responses = executeWithFallback(
                // OpenSearch operation
                () -> searchService.getAllSkills(search, level),
                // Database fallback
                () -> getAllSkillsFromDB(search, level),
                "getAllSkills");
        return enrichSkillResponses(responses);
    }

    private List<SkillResponse> getAllSkillsFromDB(String search, String level) {
        List<Skill> skills;
        if (search != null && !search.isEmpty()) {
            skills = repository.findByTitleContainingIgnoreCaseAndStatus(
                    search, SkillStatus.ACTIVE);
        } else if (level != null && !level.isEmpty()) {
            skills = repository.findByLevelAndStatus(level, SkillStatus.ACTIVE);
        } else {
            skills = repository.findByStatus(SkillStatus.ACTIVE);
        }
        return skills.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SkillResponse getSkillById(UUID id) {
        log.info("Fetching skill by id: {}", id);
        Skill skill = repository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found: " + id));
        return enrichSingleResponse(toResponse(skill));
    }

    public List<SkillResponse> getSkillsByUserId(UUID userId) {
        List<SkillResponse> responses = executeWithFallback(
                // OpenSearch operation
                () -> searchService.getSkillsByUserId(userId),
                // Database fallback
                () -> {
                    List<Skill> skills = repository.findAllByUserId(userId);
                    return skills.stream()
                            .map(this::toResponse)
                            .collect(Collectors.toList());
                },
                "getSkillsByUserId");
        return enrichSkillResponses(responses);
    }

    public List<SkillResponse> searchSkills(String query) {
        log.info("Searching skills with query: {}", query);

        // 1. Get skills from OpenSearch (or DB fallback)
        List<SkillResponse> skillResults = executeWithFallback(
                () -> searchService.searchSkillsByQuery(query),
                () -> searchSkillsFromDB(query),
                "searchSkills");

        // 2. Get skills by Mentor (Search Users -> OpenSearch Skills fallback to DB)
        List<SkillResponse> mentorResults = searchSkillsByMentor(query);

        // 3. Merge results (using a Map to ensure uniqueness by ID)
        Map<UUID, SkillResponse> uniqueSkills = new HashMap<>();

        if (skillResults != null) {
            skillResults.forEach(s -> uniqueSkills.put(s.getId(), s));
        }

        if (mentorResults != null) {
            mentorResults.forEach(s -> uniqueSkills.put(s.getId(), s));
        }

        return enrichSkillResponses(new ArrayList<>(uniqueSkills.values()));
    }

    private List<SkillResponse> searchSkillsFromDB(String query) {
        log.info("Searching in database with query: {}", query);
        List<Skill> skills = repository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);

        return skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private List<SkillResponse> searchSkillsByMentor(String query) {
        log.info("Searching skills by mentor name: {}", query);
        List<SkillResponse> providerMatches = new ArrayList<>();
        try {
            // Find users matching the name
            List<Map<String, Object>> users = userClient.searchUsers(query);
            if (users != null && !users.isEmpty()) {
                log.info("Found {} users matching query '{}'", users.size(), query);
                List<UUID> userIds = users.stream()
                        .map(u -> UUID.fromString(String.valueOf(u.get("userId"))))
                        .collect(Collectors.toList());

                log.info("Searching skills for user IDs: {}", userIds);

                // For each user, fetch their skills using OpenSearch (with DB fallback)
                for (UUID uid : userIds) {
                    List<SkillResponse> skills = executeWithFallback(
                            () -> searchService.getSkillsByUserId(uid),
                            () -> repository.findAllByUserId(uid).stream()
                                    .map(this::toResponse)
                                    .collect(Collectors.toList()),
                            "getSkillsByUserId-" + uid);
                    providerMatches.addAll(skills);
                }
            } else {
                log.info("No users found matching query '{}'", query);
            }
        } catch (Exception e) {
            log.error("Failed to search skills by provider: {}", e.getMessage());
        }

        return providerMatches.stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    // ==================== TEACHER METHODS ====================

    @Transactional
    public SkillResponse createSkill(SkillRequest req, UUID teacherId) {
        log.info("Teacher {} creating skill: {}", teacherId, req.getTitle());

        // Validate teacher exists
        if (!webClient.validateUser(teacherId)) {
            throw new UserNotFound("Invalid user id: " + teacherId);
        }

        // Build skill entity
        Skill skill = Skill.builder()
                .userId(teacherId)
                .title(req.getTitle())
                .description(req.getDescription())
                .level(req.getLevel())
                .pricePerHour(req.getPricePerHour())
                .status(SkillStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Set tags properly for @ElementCollection
        if (req.getTags() != null && !req.getTags().isEmpty()) {
            skill.setTags(new ArrayList<>(req.getTags()));
        }

        // Save to database
        Skill savedSkill = repository.save(skill);
        log.info("Skill created with id: {}", savedSkill.getId());

        // Index in OpenSearch
        try {
            indexer.indexSkills(savedSkill);
        } catch (Exception e) {
            log.error("Failed to index skill: {}", e.getMessage());
        }

        // Publish Kafka event
        try {
            producer.publishSkillCreated(savedSkill, kafkaProperties.getTopic().getSkillEvents());
        } catch (Exception e) {
            log.error("Failed to publish skill created event: {}", e.getMessage());
        }

        return enrichSingleResponse(toResponse(savedSkill));
    }

    @Transactional
    public SkillResponse updateSkill(UUID skillId, SkillRequest req, UUID teacherId) {
        log.info("Teacher {} updating skill: {}", teacherId, skillId);

        // Find skill
        Skill skill = repository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found: " + skillId));

        // Check ownership - only owner can update
        if (!skill.getUserId().equals(teacherId)) {
            throw new AccessDeniedException("You can only update your own skills");
        }

        // Update fields
        skill.setTitle(req.getTitle());
        skill.setDescription(req.getDescription());
        skill.setLevel(req.getLevel());
        skill.setPricePerHour(req.getPricePerHour());
        skill.setUpdatedAt(Instant.now());

        // Update tags
        if (req.getTags() != null && !req.getTags().isEmpty()) {
            skill.setTags(new ArrayList<>(req.getTags()));
        }

        // Save
        Skill savedSkill = repository.save(skill);
        log.info("Skill updated: {}", savedSkill.getId());

        // Re-index in OpenSearch
        try {
            indexer.indexSkills(savedSkill);
        } catch (Exception e) {
            log.error("Failed to update skill index: {}", e.getMessage());
        }

        // Publish update event
        try {
            producer.publishSkillUpdate(savedSkill, kafkaProperties.getTopic().getSkillEvents());
        } catch (Exception e) {
            log.error("Failed to publish skill update event: {}", e.getMessage());
        }

        return enrichSingleResponse(toResponse(savedSkill));
    }

    @Transactional
    public void deleteSkill(UUID skillId, UUID teacherId) {
        log.info("Teacher {} deleting skill: {}", teacherId, skillId);

        // Find skill and verify ownership
        Skill skill = repository.findByIdAndUserId(skillId, teacherId)
                .orElseThrow(() -> new SkillNotFoundException(
                        "Skill not found or you don't have permission to delete it"));

        try {
            // Delete from OpenSearch index
            indexer.deleteSkill(skillId);

            // Delete from database
            repository.delete(skill);
            log.info("Skill deleted: {}", skillId);

            // Publish deletion event
            producer.publishSkillDeleted(skillId, teacherId, kafkaProperties.getTopic().getSkillEvents());

        } catch (Exception e) {
            log.error("Failed to delete skill: {}", e.getMessage());
            throw new SkillDeletionException("Failed to delete skill", e);
        }
    }

    // ==================== ADMIN METHODS ====================

    public List<SkillResponse> getAllSkillsAdmin() {
        log.info("Admin fetching all skills (including inactive)");
        List<SkillResponse> responses = repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return enrichSkillResponses(responses);
    }

    public Map<String, Object> getSkillStats() {
        log.info("Admin fetching skill statistics");
        Map<String, Object> stats = new HashMap<>();

        List<Skill> allSkills = repository.findAll();

        // Total counts
        stats.put("total", allSkills.size());
        stats.put("active", allSkills.stream()
                .filter(s -> s.getStatus() == SkillStatus.ACTIVE)
                .count());
        stats.put("inactive", allSkills.stream()
                .filter(s -> s.getStatus() == SkillStatus.INACTIVE)
                .count());

        // Group by level
        Map<String, Long> byLevel = allSkills.stream()
                .collect(Collectors.groupingBy(Skill::getLevel, Collectors.counting()));
        stats.put("byLevel", byLevel);

        // Top teachers by skill count
        Map<UUID, Long> topTeachers = allSkills.stream()
                .collect(Collectors.groupingBy(Skill::getUserId, Collectors.counting()));
        stats.put("topTeachers", topTeachers);

        return stats;
    }

    // ==================== HELPER METHODS ====================

    private SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .userId(skill.getUserId())
                .title(skill.getTitle())
                .description(skill.getDescription())
                .tags(skill.getTags())
                .level(skill.getLevel())
                .pricePerHour(skill.getPricePerHour())
                .status(String.valueOf(skill.getStatus()))
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    private List<SkillResponse> enrichSkillResponses(List<SkillResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> userIds = responses.stream()
                .map(SkillResponse::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, String> providerNames = new HashMap<>();
        for (UUID userId : userIds) {
            try {
                Map<String, Object> userDetails = userClient.getUserDetails(userId);
                String name = null;
                if (userDetails != null) {
                    if (userDetails.get("displayName") != null) {
                        name = (String) userDetails.get("displayName");
                    } else if (userDetails.get("name") != null) {
                        name = (String) userDetails.get("name");
                    }
                }

                if (name == null) {
                    name = "Mentor " + userId.toString().substring(0, 8);
                }
                providerNames.put(userId, name);
            } catch (Exception e) {
                log.error("Failed to fetch user details for {}", userId, e);
                providerNames.put(userId, "Mentor " + userId.toString().substring(0, 8));
            }
        }

        responses.forEach(response -> {
            String name = providerNames.getOrDefault(response.getUserId(), "Unknown Mentor");
            response.setProviderName(name);
        });

        return responses;
    }

    private SkillResponse enrichSingleResponse(SkillResponse response) {
        if (response == null)
            return null;
        return enrichSkillResponses(Collections.singletonList(response)).get(0);
    }

    private <T> T executeWithFallback(
            Supplier<T> openSearchOperation,
            Supplier<T> databaseOperation,
            String operationName) {
        try {
            log.debug("Attempting {} via OpenSearch", operationName);
            return openSearchOperation.get();
        } catch (Exception e) {
            log.warn("OpenSearch failed for {}, falling back to database: {}",
                    operationName, e.getMessage());
            return databaseOperation.get();
        }
    }

}
