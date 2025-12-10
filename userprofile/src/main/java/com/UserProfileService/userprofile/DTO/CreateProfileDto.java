package com.UserProfileService.userprofile.DTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateProfileDto(

        String displayName,
        String bio,
        String city,
        List<String> skills,
        Map<String, Object> availability,
        Map<String, Object> extras,
        boolean isProvider
) {
}
