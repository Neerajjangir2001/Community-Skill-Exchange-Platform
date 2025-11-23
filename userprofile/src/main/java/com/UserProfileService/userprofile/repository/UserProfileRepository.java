package com.UserProfileService.userprofile.repository;

import com.UserProfileService.userprofile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
