package com.notification_service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.DTO.MessageReceivedEvent;
import com.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "message-notifications", groupId = "notification-service-group",  containerFactory = "kafkaListenerContainerFactory")
    public void handleMessageNotification(MessageReceivedEvent event) {
        log.info(" KAFKA: Received message notification event");
        log.info(" From: {} ({})", event.getSenderName(), event.getSenderId());
        log.info(" To: {} ({})", event.getReceiverEmail(), event.getReceiverId());
        log.info(" Content: {}", event.getMessageContent());

        try {
            // Send notification via NotificationService
            notificationService.sendNewMessageNotification(event);

            log.info(" Message notification sent successfully");
        } catch (Exception e) {
            log.error(" Failed to send message notification: {}", e.getMessage(), e);
        }
    }
}
