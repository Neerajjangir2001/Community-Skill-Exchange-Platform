package com.authService.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

//    @NotBlank
//    @Column(nullable = false)
//    private String username;

    @NotBlank
    @Column(length = 100, unique = true, nullable = false)
    private String password;

//    @NotBlank
//    @Column(nullable = false,length = 100)
//    private String displayName;

    @NotBlank
    @Email
    @Column(nullable = false)
    private String email;


    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 50)
    private Set<Role> roles = new HashSet<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

//    @Column
//    private Instant revokedAt;


    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private boolean enabled = true;
}
