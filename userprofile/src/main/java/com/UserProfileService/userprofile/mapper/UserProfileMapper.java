package com.UserProfileService.userprofile.mapper;

import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.model.UserProfile;
import com.UserProfileService.userprofile.service.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {
    private final AuthClient authClient;

    public ProfileDto toDto(UserProfile e) {
        // Fetch email from Auth Service
        String email = authClient.getUserEmail(e.getUserId());

        return new ProfileDto(
                e.getId(),
                e.getUserId(),
                e.getDisplayName(),
                email,                  // ✅ Email from Auth Service
                e.getBio(),
                e.getCity(),
                e.getAvatarUrl(),
                e.getSkills(),
                e.getAvailability(),
                e.getExtras(),
                e.isProvider()
        );
    }
}
