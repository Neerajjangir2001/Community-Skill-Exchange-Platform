package com.chat_service.controller;

import com.chat_service.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/chat/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final UserPresenceService presenceService;

    @GetMapping
    public ResponseEntity<Set<String>> getOnlineUsers() {
        return ResponseEntity.ok(presenceService.getOnlineUsers());
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> sendHeartbeat(
            @org.springframework.security.core.annotation.AuthenticationPrincipal String userId) {
        // Fallback if userId is not directly injected as String, try SecurityContext
        if (userId == null) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null) {
                userId = auth.getName(); // This usually corresponds to Subject/User ID
            }
        }

        if (userId != null) {
            presenceService.updateHeartbeat(userId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }
}
