package com.UserProfileService.userprofile.DTO;

import java.util.List;
import java.util.Map;

public record UpdateProfileDto(
        String displayName,
        String bio,
        String city,
        List<String> skills,
        Map<String, Object> availability,
        Map<String, Object> extras,
        Boolean isProvider
) {
}
