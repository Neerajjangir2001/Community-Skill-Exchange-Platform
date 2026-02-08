package com.SkillCatalogService.skillservice.repository;

import com.SkillCatalogService.skillservice.model.Skill;
import com.SkillCatalogService.skillservice.model.SkillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    // Find by user
    List<Skill> findAllByUserId(UUID userId);

    List<Skill> findAllByUserIdIn(List<UUID> userIds);

    Optional<Skill> findByIdAndUserId(UUID id, UUID userId);

    // Find by status
    List<Skill> findByStatus(SkillStatus status);

    // Search methods
    List<Skill> findByTitleContainingIgnoreCaseAndStatus(String title, SkillStatus status);

    List<Skill> findByLevelAndStatus(String level, SkillStatus status);

    List<Skill> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    void deleteByUserId(UUID userId);

}
