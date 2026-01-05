package com.notification_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.DTO.SkillMatchFoundEvent;
import com.notification_service.DTO.UserRegisteredEvent;
import com.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user.registered", groupId = "notification-service-group")
    public void consumeUserRegistered(String message) {
        try {
            log.info(" Received user.registered event: {}", message);

            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);
            notificationService.sendWelcomeEmail(event);

            log.info(" Successfully processed user.registered event for user ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error(" Error processing user.registered event", e);
        }
    }

    @KafkaListener(topics = "skill.match.found", groupId = "notification-service-group")
    public void consumeSkillMatchFound(String message) {
        try {
            log.info(" Received skill.match.found event: {}", message);

            SkillMatchFoundEvent event = objectMapper.readValue(message, SkillMatchFoundEvent.class);
            notificationService.sendSkillMatchNotification(event);

            log.info(" Successfully processed skill.match.found event for match ID: {}", event.getMatchId());
        } catch (Exception e) {
            log.error(" Error processing skill.match.found event", e);
        }
    }
}