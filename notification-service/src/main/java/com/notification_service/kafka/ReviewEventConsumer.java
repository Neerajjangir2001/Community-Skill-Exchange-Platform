package com.notification_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.DTO.ReviewCreatedEvent;
import com.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewEventConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "review.created", groupId = "notification-service-group")
    public void consumeReviewCreated(String message) {
        try {
            log.info(" Received review.created event: {}", message);

            ReviewCreatedEvent event = objectMapper.readValue(message, ReviewCreatedEvent.class);
            notificationService.sendNewReviewNotification(event);

            log.info(" Successfully processed review.created event for review ID: {}", event.getReviewId());
        } catch (Exception e) {
            log.error(" Error processing review.created event", e);
        }
    }
}
