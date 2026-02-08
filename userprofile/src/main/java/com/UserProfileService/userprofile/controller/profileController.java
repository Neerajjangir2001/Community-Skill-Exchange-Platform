package com.UserProfileService.userprofile.controller;

import com.UserProfileService.userprofile.DTO.CreateProfileDto;
import com.UserProfileService.userprofile.DTO.ProfileDto;
import com.UserProfileService.userprofile.DTO.UpdateProfileDto;
import com.UserProfileService.userprofile.service.CloudinaryService;
import com.UserProfileService.userprofile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class profileController {

    private final ProfileService profileService;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateProfileDto dto, Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        profileService.createProfile(dto, userId);
        return ResponseEntity.ok("Profile Created Successfully");

    }

    @GetMapping("/userComeByUserId")
    public ResponseEntity<ProfileDto> getByUser(Authentication authentication) {

        String authenticatedUserId = authentication.getName();
        UUID uuid = UUID.fromString(String.valueOf(authenticatedUserId));

        ProfileDto dto = profileService.getByUserId(uuid);
        if (dto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);

    }

    @PutMapping("/update")
    public ResponseEntity<ProfileDto> update(@RequestBody UpdateProfileDto dto, Authentication authentication) {

        String authenticatedUserId = authentication.getName();
        UUID user = UUID.fromString(String.valueOf(authenticatedUserId));
        ProfileDto profileDto = profileService.updateProfile(dto, user);
        return ResponseEntity.ok(profileDto);
    }

    @PostMapping("/users/me/avatar")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        UUID userId = UUID.fromString(authenticatedUserId);

        String url = cloudinaryService.uploadAvatar(userId, file);
        profileService.updateAvatar(userId, url);

        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProfileDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean isProvider) {
        return ResponseEntity.ok(profileService.searchProfiles(keyword, city, isProvider));
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable UUID id) {
        boolean exists = profileService.validateUser(id);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileDto> getProfileById(@PathVariable UUID userId) {
        ProfileDto profile = profileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable UUID userId, Authentication authentication) {
        String authenticatedUserId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!authenticatedUserId.equals(userId.toString()) && !isAdmin) {
            return ResponseEntity.status(403).build();
        }

        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
