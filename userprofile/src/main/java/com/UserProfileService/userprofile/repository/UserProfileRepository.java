package com.UserProfileService.userprofile.repository;

import com.UserProfileService.userprofile.model.UserProfile;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);


    @Query("""
    SELECT p FROM UserProfile p
    LEFT JOIN FETCH p.skills
    WHERE 
        (:keyword IS NULL OR 
            LOWER(CAST(p.displayName AS text)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS text)) OR
            LOWER(CAST(p.bio AS text)) LIKE LOWER(CAST(CONCAT('%', :keyword, '%') AS text))
        )
        AND (:city IS NULL OR LOWER(CAST(p.city AS text)) = LOWER(CAST(:city AS text)))
        AND (:isProvider IS NULL OR p.isProvider = :isProvider)
""")

    List<UserProfile> searchProfiles(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("isProvider") Boolean isProvider
    );


}
