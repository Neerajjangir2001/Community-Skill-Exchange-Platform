package com.notification_service.service;

import com.notification_service.entity.PushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;



    public void sendToUser(UUID userId, PushNotification notification) {
        try {
            log.info(" Sending WebSocket push notification to user: {}", userId);

                    notification.setId(UUID.randomUUID());
                    notification.setTimestamp(LocalDateTime.now());

                    messagingTemplate.convertAndSendToUser(
                            userId.toString(),
                            "/topic/notifications", notification);
            log.info(" WebSocket push notification sent to user: {}", userId);

        }catch (Exception e) {
            log.error(" Failed to send WebSocket push notification to user {}: {}", userId, e.getMessage());
        }
    }

    public void sendToAll(PushNotification notification) {
        try {
            log.info(" Broadcasting WebSocket notification to all users");

            notification.setId(UUID.randomUUID());
            notification.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSend(
                    "/topic/notifications",
                    notification
            );

            log.info(" Broadcast notification sent");
        } catch (Exception e) {
            log.error(" Failed to broadcast notification: {}", e.getMessage());
        }
    }

    public void sendToTopic(String topic, PushNotification notification) {
        try {
            log.info(" Sending notification to topic: {}", topic);

            notification.setId(UUID.randomUUID());
            notification.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSend(
                    "/topic/" + topic,
                    notification
            );

            log.info(" Topic notification sent: {}", topic);
        } catch (Exception e) {
            log.error(" Failed to send topic notification: {}", e.getMessage());
        }
    }

}
