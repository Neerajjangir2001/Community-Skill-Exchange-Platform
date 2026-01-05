package com.UserProfileService.userprofile.DTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProfileDto(
        UUID id,
        UUID userId,
        String displayName,
        String email,
        String bio,
        String city,
        String avatarUrl,
        List<String> skills,
        Map<String, Object> availability,
        Map<String, Object> extras,
        boolean isProvider
) {
}
