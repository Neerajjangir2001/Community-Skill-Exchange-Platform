package com.notification_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotification {
    private UUID id;
    private String title;
    private String message;
    private String type; // BOOKING, REVIEW, MESSAGE, SKILL_MATCH, USER
    private UUID recipientId;
    private LocalDateTime timestamp;
    private String actionUrl;
    private NotificationPriority priority;
    private String referenceId;
}
