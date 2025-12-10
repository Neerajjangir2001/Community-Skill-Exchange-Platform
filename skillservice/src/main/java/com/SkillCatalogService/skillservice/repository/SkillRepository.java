package com.SkillCatalogService.skillservice.repository;

import com.SkillCatalogService.skillservice.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByUserId(UUID userId);

    List<Skill> findAllByUserId(UUID userId);


    void deleteByUserId(UUID id);

    Optional<Skill> findByIdAndUserId(UUID id, UUID userId);
}
