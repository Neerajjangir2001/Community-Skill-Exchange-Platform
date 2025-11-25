package com.UserProfileService.userprofile.controller;

import com.UserProfileService.userprofile.DTO.CreateProfileDto;
import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.DTO.UpdateProfileDto;
import com.UserProfileService.userprofile.service.CloudinaryService;
import com.UserProfileService.userprofile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/userProfile")
@RequiredArgsConstructor
public class profileController {


    private final ProfileService profileService;
    private final CloudinaryService cloudinaryService;


    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateProfileDto dto) {
        profileService.createProfile(dto);
        return  ResponseEntity.ok("Profile Created Successfully");

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
