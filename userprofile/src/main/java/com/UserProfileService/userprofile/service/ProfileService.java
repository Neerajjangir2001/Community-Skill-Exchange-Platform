package com.UserProfileService.userprofile.service;


import com.UserProfileService.userprofile.DTO.CreateProfileDto;
import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.DTO.UpdateProfileDto;
import com.UserProfileService.userprofile.mapper.UserProfileMapper;
import com.UserProfileService.userprofile.model.UserProfile;
import com.UserProfileService.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AuthClient webClient;
//    private final ModelMapper modelMapper;
    private final UserProfileMapper userProfileMapper;



    public ProfileDto createProfile(CreateProfileDto profileDto) {

        if (!webClient.validateUser(profileDto.userId())){
            throw new RuntimeException("Invalid user id");
        }

        if (userProfileRepository.findByUserId(profileDto.userId()).isPresent()){
            throw new RuntimeException("User already exists");
        }

        UserProfile userProfile = UserProfile.builder()
                .userId(profileDto.userId())
                .displayName(profileDto.displayName())
                .bio(profileDto.bio())
                .skills(profileDto.skills())
                .availability(profileDto.availability())
                .extras(profileDto.extras())
                .isProvider(profileDto.isProvider())
                .city(profileDto.city())
                .build();
    return  userProfileMapper.toDto(userProfileRepository.save(userProfile));

    }
    public ProfileDto updateProfile(UpdateProfileDto dto ,  UUID userId) {
        if (!webClient.validateUser(userId))
            throw new IllegalArgumentException("User does not exist");

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));


        if (dto.displayName() != null) userProfile.setDisplayName(dto.displayName());
        if (dto.bio() != null) userProfile.setBio(dto.bio());
        if (dto.city() != null) userProfile.setCity(dto.city());
        if (dto.skills() != null) userProfile.setSkills(dto.skills());
        if (dto.availability() != null) userProfile.setAvailability(dto.availability());
        if (dto.extras() != null) userProfile.setExtras(dto.extras());
        if (dto.isProvider() != null) userProfile.setProvider(dto.isProvider());

        return  userProfileMapper.toDto(userProfileRepository.save(userProfile));

    }

    public ProfileDto updateAvatar(UUID userId, String avatarUrl) {

        if (!webClient.validateUser(userId))
            throw new IllegalArgumentException("User does not exist");

        UserProfile p = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        p.setAvatarUrl(avatarUrl);
        return userProfileMapper.toDto(userProfileRepository.save(p));
    }


    public void deleteProfile(UUID userId) {
        userProfileRepository.deleteByUserId(userId);
    }

    public ProfileDto getByUserId(UUID userId) {

        if (!webClient.validateUser(userId))
            throw new IllegalArgumentException("User does not exist");

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        return userProfileMapper.toDto(userProfile);


    }
}
