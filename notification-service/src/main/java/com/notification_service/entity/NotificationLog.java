package com.notification_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notification_logs")
public class NotificationLog {
    @Id
    private String id;

    private String userId;
    private String userEmail;
    private NotificationType type;
    private String channel; // BOOKING, REVIEW, MESSAGE, USER, MATCHING
    private String subject;
    private String content;
    private NotificationStatus status;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    private String referenceId; // UUID as String
    private String referenceType; // BOOKING, REVIEW, MESSAGE, USER, MATCH


}
