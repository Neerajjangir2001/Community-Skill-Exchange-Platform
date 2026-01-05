package com.notification_service.repository;

import com.notification_service.entity.NotificationLog;
import com.notification_service.entity.NotificationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    List<NotificationLog> findByUserId(String userId);

    List<NotificationLog> findByUserIdAndStatus(UUID userId, NotificationStatus status);

    List<NotificationLog> findByStatus(NotificationStatus status);

    List<NotificationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<NotificationLog> findByReferenceIdAndReferenceType(String referenceId, String referenceType);

    List<NotificationLog> findByChannelAndStatus(String channel, NotificationStatus status);

    long countByUserIdAndStatus(UUID userId, NotificationStatus status);
}
