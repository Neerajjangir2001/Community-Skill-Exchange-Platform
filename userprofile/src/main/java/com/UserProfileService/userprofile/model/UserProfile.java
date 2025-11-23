package com.UserProfileService.userprofile.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_profiles")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {


    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    private String displayName;
    private String bio;
    private String city;
    private String avatarUrl;

    @ElementCollection
    private List<String> skills;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> availability;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> extras;

    private boolean isProvider;


    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();


    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }

    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
