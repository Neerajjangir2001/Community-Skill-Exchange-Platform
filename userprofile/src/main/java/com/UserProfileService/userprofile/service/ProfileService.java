package com.UserProfileService.userprofile.service;

import com.UserProfileService.userprofile.DTO.CreateProfileDto;
import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.DTO.UpdateProfileDto;
import com.UserProfileService.userprofile.event.UserDeletedEvent;
import com.UserProfileService.userprofile.event.UserRegisteredEvent;
import com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles.ProfileNotFoundException;
import com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles.UserAlreadyExistsException;
import com.UserProfileService.userprofile.exceptionHandle.allExceprionHandles.UserNotFound;
import com.UserProfileService.userprofile.mapper.UserProfileMapper;
import com.UserProfileService.userprofile.model.UserProfile;
import com.UserProfileService.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {


    @Value("${kafka.topic.user-deleted}")
    private String userDeletedTopic;

    @Value("${kafka.topic.user-registered}")
    private String userRegisteredTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserProfileRepository userProfileRepository;
    private final AuthClient webClient;
    private final UserProfileMapper userProfileMapper;

    public ProfileDto createProfile(CreateProfileDto profileDto, UUID userId) {

        if (!webClient.validateUser(userId)) {
            throw new UserNotFound("Invalid user id");
        }

        if (userProfileRepository.findByUserId(userId).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }

        UserProfile userProfile = UserProfile.builder()
                .userId(userId)
                .displayName(profileDto.displayName())
                .bio(profileDto.bio())
                .skills(profileDto.skills())
                .availability(profileDto.availability())
                .extras(profileDto.extras())
                .isProvider(profileDto.isProvider())
                .city(profileDto.city())
                .build();
        UserProfile savedProfile = userProfileRepository.save(userProfile);

        // Publish UserRegisteredEvent for Welcome Email
        try {
            String email = webClient.getUserEmail(userId);
            UserRegisteredEvent event = new UserRegisteredEvent(
                    userId,
                    savedProfile.getDisplayName(), // Now we have the actual name!
                    email,
                  LocalDateTime.now());
            kafkaTemplate.send(userRegisteredTopic, userId.toString(), event);
        } catch (Exception e) {
            // Log error but don't fail profile creation
            System.err.println("Failed to send UserRegisteredEvent: " + e.getMessage());
        }

        return userProfileMapper.toDto(savedProfile);

    }

    public ProfileDto updateProfile(UpdateProfileDto dto, UUID userId) {
        if (!webClient.validateUser(userId))
            throw new UserNotFound("User does not exist");

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(ProfileNotFoundException::new);

        if (dto.displayName() != null)
            userProfile.setDisplayName(dto.displayName());
        if (dto.bio() != null)
            userProfile.setBio(dto.bio());
        if (dto.city() != null)
            userProfile.setCity(dto.city());
        if (dto.skills() != null)
            userProfile.setSkills(dto.skills());
        if (dto.availability() != null)
            userProfile.setAvailability(dto.availability());
        if (dto.extras() != null)
            userProfile.setExtras(dto.extras());
        if (dto.isProvider() != null)
            userProfile.setProvider(dto.isProvider());

        return userProfileMapper.toDto(userProfileRepository.save(userProfile));

    }

    public ProfileDto updateAvatar(UUID userId, String avatarUrl) {

        if (!webClient.validateUser(userId))
            throw new UserNotFound("User does not exist");

        UserProfile p = userProfileRepository.findByUserId(userId)
                .orElseThrow(ProfileNotFoundException::new);

        p.setAvatarUrl(avatarUrl);
        return userProfileMapper.toDto(userProfileRepository.save(p));
    }



    @Transactional
    public void deleteProfile(UUID userId) {
        userProfileRepository.deleteByUserId(userId);
        webClient.deleteUser(userId);

        // Publish event
        UserDeletedEvent event = new UserDeletedEvent(
                userId, System.currentTimeMillis());
        kafkaTemplate.send(userDeletedTopic, userId.toString(), event);
    }

    public ProfileDto getByUserId(UUID userId) {

        if (!webClient.validateUser(userId))
            throw new UserNotFound("User does not exist");

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(ProfileNotFoundException::new);

        return userProfileMapper.toDto(userProfile);

    }

    public List<ProfileDto> searchProfiles(String keyword, String city, Boolean isProvider) {
        List<UserProfile> results = userProfileRepository.searchProfiles(keyword, city, isProvider);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.stream()
                .map(userProfileMapper::toDto).toList();

    }

    public Boolean validateUser(UUID userId) {

        if (!webClient.validateUser(userId))
            throw new UserNotFound("User does not exist");

        return true;

    }

    public ProfileDto getProfileByUserId(UUID userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(ProfileNotFoundException::new);
        return userProfileMapper.toDto(userProfile);
    }

}
