package com.SkillCatalogService.skillservice.controller;

import com.SkillCatalogService.skillservice.DTO.SkillRequest;
import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {
    private final SkillService service;
    private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SkillRequest req,
                                                @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {

        UUID userId;
        if (userIdHeader != null) {
            userId = UUID.fromString(userIdHeader);
        } else {
            throw new RuntimeException("User ID not found");
        }
        SkillResponse skill = service.createSkill(req, userId);
        return ResponseEntity.ok( skill);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/my-skills")
    public ResponseEntity<List<SkillResponse>> getMySkills(
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SkillRequest req,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        return ResponseEntity.ok(service.update(id, req, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteSkill(@PathVariable UUID id,
                                                           Authentication authentication) {

        // Get userId from JWT token (secure - not from header)
        UUID userId = UUID.fromString(authentication.getName());

        service.delete(id, userId);

        logger.info("Skill {} deleted by user {}", id, userId);

        return ResponseEntity.ok(Map.of(
                "message", "Skill deleted successfully",
                "skillId", id.toString()
        ));
    }

    private UUID extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return UUID.fromString(auth.getName());
    }

//    private boolean hasRole(Authentication auth, String role) {
//        if (auth == null) return false;
//        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals( role) || a.getAuthority().equals(role));
//    }
}
