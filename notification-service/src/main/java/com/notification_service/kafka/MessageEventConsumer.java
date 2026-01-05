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

    @KafkaListener(topics = "message.received", groupId = "notification-service-group")
    public void consumeMessageReceived(String message) {
        try {
            log.info(" Received message.received event: {}", message);

            MessageReceivedEvent event = objectMapper.readValue(message, MessageReceivedEvent.class);
            notificationService.sendNewMessageNotification(event);

            log.info(" Successfully processed message.received event for message ID: {}", event.getMessageId());
        } catch (Exception e) {
            log.error(" Error processing message.received event", e);
        }
    }
}
