package com.SkillCatalogService.skillservice.controller;

import com.SkillCatalogService.skillservice.DTO.SkillRequest;
import com.SkillCatalogService.skillservice.DTO.SkillResponse;
import com.SkillCatalogService.skillservice.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@Slf4j
public class SkillController {
    private final SkillService skillService;




    // ==================== TEACHER ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<SkillResponse> createSkill(
            Authentication authentication,
            @Valid @RequestBody SkillRequest request) {

        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("POST /api/skills - Teacher: {}, Title: {}", teacherId, request.getTitle());

        SkillResponse response = skillService.createSkill(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-skills")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<SkillResponse>> getMySkills(Authentication authentication) {
        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("GET /api/skills/my-skills - Teacher: {}", teacherId);
        return ResponseEntity.ok(skillService.getSkillsByUserId(teacherId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable UUID id,
            @Valid @RequestBody SkillRequest request,
            Authentication authentication) {

        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("PUT /api/skills/{} - Teacher: {}", id, teacherId);

        SkillResponse response = skillService.updateSkill(id, request, teacherId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("DELETE /api/skills/{} - Teacher: {}", id, teacherId);

        skillService.deleteSkill(id, teacherId);
        return ResponseEntity.noContent().build();
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SkillResponse>> getAllSkillsAdmin() {
        log.info("GET /api/skills/all - Admin request");
        return ResponseEntity.ok(skillService.getAllSkillsAdmin());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSkillStats() {
        log.info("GET /api/skills/stats - Admin request");
        return ResponseEntity.ok(skillService.getSkillStats());
    }
}
