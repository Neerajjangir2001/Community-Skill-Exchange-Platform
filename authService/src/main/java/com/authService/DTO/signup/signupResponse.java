package com.authService.DTO.signup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class signupResponse {
    private UUID id;
    private String email;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

}
