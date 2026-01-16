package com.notification_service.service;

import com.notification_service.entity.NotificationLog;
import com.notification_service.entity.NotificationStatus;
import com.notification_service.entity.NotificationType;
import com.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationLogService {
    private final NotificationLogRepository notificationLogRepository;

//Save notification log
    public void saveLog(UUID userId, String email, String type, String subject,
                        String content, String referenceId) {
        try {
            NotificationLog log = NotificationLog.builder()
                    .userId(userId.toString())
                    .userEmail(email)
                    .type(NotificationType.EMAIL) // Can be parameterized
                    .channel(type)
                    .subject(subject)
                    .content(content)
                    .status(NotificationStatus.SENT)
                    .referenceId(referenceId)
                    .referenceType(type)
                    .sentAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationLogRepository.save(log);
//            log.debug(" Notification log saved for user: {}", userId);

        } catch (Exception e) {
            log.error(" Failed to save notification log: {}", e.getMessage());
        }
    }
}
