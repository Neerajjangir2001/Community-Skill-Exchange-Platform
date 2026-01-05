package com.notification_service.controller;


import com.notification_service.entity.NotificationLog;
import com.notification_service.entity.NotificationStatus;
import com.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationLog>> getUserNotifications(@PathVariable String userId) {
        List<NotificationLog> notifications = notificationLogRepository.findByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<NotificationLog>> getUserNotificationsByStatus(
            @PathVariable UUID userId,
            @PathVariable NotificationStatus status) {
        List<NotificationLog> notifications = notificationLogRepository.findByUserIdAndStatus(userId, status);
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

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationLog>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        List<NotificationLog> notifications = notificationLogRepository.findByStatus(status);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/channel/{channel}")
    public ResponseEntity<List<NotificationLog>> getNotificationsByChannel(
            @PathVariable String channel,
            @RequestParam(required = false) NotificationStatus status) {

        List<NotificationLog> notifications;
        if (status != null) {
            notifications = notificationLogRepository.findByChannelAndStatus(channel, status);
        } else {
            notifications = notificationLogRepository.findAll().stream()
                    .filter(n -> n.getChannel().equals(channel))
                    .toList();
        }
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    public ResponseEntity<List<NotificationLog>> getAllNotifications() {
        List<NotificationLog> notifications = notificationLogRepository.findAll();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<NotificationLog>> getNotificationsByReference(
            @PathVariable String referenceType,
            @PathVariable String referenceId) {
        List<NotificationLog> notifications = notificationLogRepository
                .findByReferenceIdAndReferenceType(referenceId, referenceType);
        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        notificationLogRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
