package com.SkillCatalogService.skillservice.service;

import com.SkillCatalogService.skillservice.DTO.SkillRequest;
import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.config.KafkaProperties;
import com.SkillCatalogService.skillservice.exceptionHandle.allExceprionHandles.*;
import com.SkillCatalogService.skillservice.kafka.SkillEventsProducer;
import com.SkillCatalogService.skillservice.model.Skill;
import com.SkillCatalogService.skillservice.model.SkillStatus;
import com.SkillCatalogService.skillservice.openSearch.SkillSearchIndexer;
import com.SkillCatalogService.skillservice.repository.SkillRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository repository;
    private final SkillSearchIndexer indexer;
    private final SkillEventsProducer producer;
    private final AuthClient webClient;
    private final SkillRepository skillRepository;
    private final KafkaProperties kafkaProperties;
//    private static final Logger logger = LoggerFactory.getLogger(SkillService.class);

    @Value("${kafka.topic.skill-events}")
    private String skillTopic;


    @Transactional
    public SkillResponse createSkill(SkillRequest req, UUID userId) {

        if (!webClient.validateUser(userId)){
            throw new UserNotFound("Invalid user id");
        }

        if (skillRepository.findByUserId(userId).isPresent()){
            throw new UserAlreadyExistsException("User already exists");
        }

        Skill skill = Skill.builder()
                .userId(userId)
                .title(req.getTitle())
                .description(req.getDescription())
                .tags(req.getTags())
                .level(req.getLevel())
                .pricePerHour(req.getPricePerHour())
                .status(SkillStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        Skill savedSkill = repository.save(skill);

        indexer.indexSkills(skill);
        producer.publishSkillCreated(savedSkill,kafkaProperties.getTopic().getSkillEvents());
        return toResponse(savedSkill);

    }

    public SkillResponse getById(UUID id) {
        Skill s = repository.findById(id).orElseThrow(() -> new ProfileNotFoundException("Skills not found"));
        return toResponse(s);
    }

    public List<SkillResponse> getByUserId(UUID userId) {
        List<Skill> skills = repository.findAllByUserId(userId);

        if (skills.isEmpty()){
            throw new SkillNotFoundException(" Skill Not found this ID: " + userId);
        }

        return skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SkillResponse update(UUID id, SkillRequest req, UUID userId) {
        Skill skill = repository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with id: " + id));


        if (!skill.getUserId().equals(userId)) {
            throw new AccessDeniedException("Only the owner can update this skill");
        }
        skill.setTitle(req.getTitle());
        skill.setDescription(req.getDescription());
        skill.setTags(req.getTags());
        skill.setLevel(req.getLevel());
        skill.setPricePerHour(req.getPricePerHour());
        skill.setUpdatedAt(Instant.now());

        Skill saved = repository.save(skill);
        indexer.indexSkills(saved);
        producer.publishSkillUpdate(saved,kafkaProperties.getTopic().getSkillEvents());
        return toResponse(saved);
    }


    @Transactional
    public void delete(UUID skillId, UUID userId) {

        Skill skill = repository.findByIdAndUserId(skillId, userId)
                .orElseThrow(() -> new SkillNotFoundException(
                        "Skill not found or you don't have permission to delete it"
                ));

        try {
            // Delete from OpenSearch index first (can rollback DB if fails)
            indexer.deleteSkill(skillId);
            repository.delete(skill);
            // Publish event to Kafka (after successful deletion)
            producer.publishSkillDeleted(
                    skillId,
                    userId,
                    kafkaProperties.getTopic().getSkillEvents()
            );

        } catch (Exception e) {
            throw new SkillDeletionException("Failed to delete skill", e);
        }
    }


    private SkillResponse toResponse(Skill s) {
        return SkillResponse.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .title(s.getTitle())
                .description(s.getDescription())
                .tags(s.getTags())
                .level(s.getLevel())
                .pricePerHour(s.getPricePerHour())
                .status(s.getStatus()!=null? s.getStatus().name():null)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }



}
