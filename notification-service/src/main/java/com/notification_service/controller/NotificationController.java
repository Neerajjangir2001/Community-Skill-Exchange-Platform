package com.notification_service.controller;


import com.notification_service.entity.NotificationLog;
import com.notification_service.entity.NotificationStatus;
import com.notification_service.entity.PushNotification;
import com.notification_service.entity.UserDeviceToken;
import com.notification_service.repository.NotificationLogRepository;
import com.notification_service.repository.UserDeviceTokenRepository;
import com.notification_service.service.OneSignalPushService;
import com.notification_service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;
    private final WebSocketNotificationService webSocketService;
    private final OneSignalPushService oneSignalPushService;
    private final UserDeviceTokenRepository deviceTokenRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // ========== DEVICE TOKEN MANAGEMENT ==========


    @PostMapping("/device/register")
    public ResponseEntity<Map<String, Object>> registerDeviceToken(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String deviceToken = request.get("deviceToken");
            String deviceType = request.get("deviceType");
            String deviceName = request.getOrDefault("deviceName", "Unknown Device");
            String osVersion = request.getOrDefault("osVersion", "Unknown OS");
            String appVersion = request.getOrDefault("appVersion", "1.0.0");

            //  Check if this user already has this token
            Optional<UserDeviceToken> existingToken = deviceTokenRepository
                    .findByUserIdAndDeviceToken(UUID.fromString(userId), deviceToken);

            if (existingToken.isPresent()) {
                //  Update existing token
                UserDeviceToken token = existingToken.get();
                token.setActive(true);
                token.setUpdatedAt(LocalDateTime.now());
                token.setLastUsed(LocalDateTime.now());
                token.setDeviceName(deviceName);
                token.setOsVersion(osVersion);
                token.setAppVersion(appVersion);
                deviceTokenRepository.save(token);

                log.info(" Updated existing token for user: {}", userId);
                return ResponseEntity.ok(Map.of(
                        "message", "Device token updated",
                        "tokenId", token.getId(),
                        "isUpdate", true
                ));
            }


            UserDeviceToken token = UserDeviceToken.builder()
                    .userId(userId)
                    .deviceToken(deviceToken)
                    .deviceType(deviceType)
                    .deviceName(deviceName)
                    .osVersion(osVersion)
                    .appVersion(appVersion)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .lastUsed(LocalDateTime.now())
                    .active(true)
                    .build();

            UserDeviceToken saved = deviceTokenRepository.save(token);
            log.info(" Created new token for user: {}", userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Device token registered",
                    "tokenId", saved.getId(),
                    "isUpdate", false
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid userId format"
            ));
        } catch (Exception e) {
            log.error(" Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to register device token",
                    "message", e.getMessage()
            ));
        }
    }


    @DeleteMapping("/device/unregister/{deviceToken}")
    public ResponseEntity<String> unregisterDeviceToken(@PathVariable String deviceToken) {
        try {
            deviceTokenRepository.findByDeviceToken(deviceToken).ifPresent(token -> {
                token.setActive(false);
                token.setUpdatedAt(LocalDateTime.now());
                deviceTokenRepository.save(token);
            });
            return ResponseEntity.ok("Device token unregistered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to unregister device token: " + e.getMessage());
        }
    }

    @GetMapping("/device/user/{userId}")
    public ResponseEntity<List<UserDeviceToken>> getUserDevices(@PathVariable String userId) {
        List<UserDeviceToken> tokens = deviceTokenRepository.findByUserIdAndActiveTrue(userId);
        return ResponseEntity.ok(tokens);
    }


    // ========== WEBSOCKET NOTIFICATIONS ==========

    @PostMapping("/websocket/send/{userId}")
    public ResponseEntity<String> sendWebSocketNotification(
            @PathVariable UUID userId,
            @RequestBody PushNotification notification) {

        webSocketService.sendToUser(userId, notification);
        return ResponseEntity.ok("WebSocket notification sent to user: " + userId);
    }

    @PostMapping("/websocket/broadcast")
    public ResponseEntity<String> broadcastWebSocketNotification(
            @RequestBody PushNotification notification) {

        webSocketService.sendToAll(notification);
        return ResponseEntity.ok("WebSocket notification broadcasted to all users");
    }






    // ========== PUSH NOTIFICATIONS ==========

    @PostMapping("/push/send-test")
    public ResponseEntity<Map<String, String>> sendTestPushNotification(
            @RequestParam String userId,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification") String message) {

        log.info(" Sending test push notification to user: {}", userId);

        Map<String, String> data = Map.of("type", "TEST", "timestamp", LocalDateTime.now().toString());
        String notificationId = oneSignalPushService.sendToUser(userId, title, message, data);

        if (notificationId != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Test notification sent",
                    "notificationId", notificationId,
                    "userId", userId.toString()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "status", "failed",
                    "message", "Failed to send notification"
            ));
        }
    }


    // ========== NOTIFICATION LOGS ==========

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationLog>> getUserNotifications(@PathVariable String userId) {
        List<NotificationLog> notifications = notificationLogRepository.findByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> getUserNotificationCount(@PathVariable UUID userId) {
        long sentCount = notificationLogRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT);
        long failedCount = notificationLogRepository.countByUserIdAndStatus(userId, NotificationStatus.FAILED);

        return ResponseEntity.ok(Map.of(
                "sent", sentCount,
                "failed", failedCount,
                "total", sentCount + failedCount
        ));
    }

    @GetMapping
    public ResponseEntity<List<NotificationLog>> getAllNotifications() {
        List<NotificationLog> notifications = notificationLogRepository.findAll();
        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        notificationLogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
