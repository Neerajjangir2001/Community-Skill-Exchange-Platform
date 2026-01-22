package com.chat_service.service;

import com.chat_service.DTO.ChatNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    @Value("${kafka.topic.message-received:message-notifications}")
    private String notificationEventsTopic;

    private final KafkaTemplate<String, ChatNotificationEvent> kafkaTemplate;

    public void sendMessageNotification(ChatNotificationEvent event) {

        try {
            kafkaTemplate.send(notificationEventsTopic, event);
            log.info(" Kafka notification sent to topic: {}", notificationEventsTopic);
        }catch (Exception e) {
            log.error(" Failed to send Kafka notification: {}", e.getMessage());
        }
    }
}
