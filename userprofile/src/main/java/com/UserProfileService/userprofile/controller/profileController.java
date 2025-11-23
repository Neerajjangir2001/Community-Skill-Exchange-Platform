package com.UserProfileService.userprofile.controller;

import com.UserProfileService.userprofile.DTO.CreateProfileDto;
import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.DTO.UpdateProfileDto;
import com.UserProfileService.userprofile.service.CloudinaryService;
import com.UserProfileService.userprofile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/userProfile")
@RequiredArgsConstructor
public class profileController {


    private final ProfileService profileService;
    private final CloudinaryService cloudinaryService;


    @PostMapping
    public ResponseEntity<ProfileDto> create(@RequestBody CreateProfileDto dto) {
        return ResponseEntity.status(201).body(profileService.createProfile(dto));

    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileDto> getByUser(@PathVariable UUID userId) {
        ProfileDto dto = profileService.getByUserId(userId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ProfileDto> update(@PathVariable UUID userId, @RequestBody UpdateProfileDto dto) {
        return ResponseEntity.ok(profileService.updateProfile(dto, userId));
    }

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<Map<String, String>> upload(@PathVariable UUID userId,
                                                      @RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadAvatar(userId, file);
        profileService.updateAvatar(userId, url);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }
}
