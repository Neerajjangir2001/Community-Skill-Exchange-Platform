package com.UserProfileService.userprofile.mapper;

import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    public ProfileDto toDto(UserProfile e) {
        return new ProfileDto(
                e.getId(),
                e.getUserId(),
                e.getDisplayName(),
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
