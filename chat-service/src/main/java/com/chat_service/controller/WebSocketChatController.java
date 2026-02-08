package com.chat_service.controller;

import com.chat_service.DTO.MessageDTO;
import com.chat_service.model.MessageType;
import com.chat_service.service.MessageService;
import com.chat_service.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserPresenceService presenceService;

    // Handle incoming chat messages via WebSocket

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, String> messageRequest) {
        String senderId = messageRequest.get("senderId");
        String receiverId = messageRequest.get("receiverId");
        String content = messageRequest.get("content");

        log.info(" WebSocket message from {} to {}: {}", senderId, receiverId, content);

        // Save message to database
        MessageDTO savedMessage = messageService.sendMessage(
                senderId,
                receiverId,
                content,
                MessageType.TEXT);

        // Send to specific user via WebSocket
        messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/messages",
                savedMessage);

        log.info(" Message sent via WebSocket to user: {}", receiverId);
    }

    // Handle typing indicator

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, String> typingData) {
        String senderId = typingData.get("senderId");
        String receiverId = typingData.get("receiverId");
        boolean isTyping = Boolean.parseBoolean(typingData.getOrDefault("isTyping", "false"));

        log.info("️ User {} typing status: {}", senderId, isTyping);

        // Notify receiver about typing status
        messagingTemplate.convertAndSendToUser(
                receiverId,
                "/queue/typing",
                Map.of("senderId", senderId, "isTyping", isTyping));
    }

    // Handle user online status
    @MessageMapping("/chat.online")
    public void handleOnlineStatus(@Payload Map<String, String> statusData) {
        String userId = statusData.get("userId");
        boolean isOnline = Boolean.parseBoolean(statusData.getOrDefault("online", "false"));

        log.info(" User {} online status: {}", userId, isOnline);

        if (isOnline) {
            presenceService.markUserOnline(userId);
        } else {
            presenceService.markUserOffline(userId);
        }

        // Broadcast online status
        messagingTemplate.convertAndSend(
                "/topic/status",
                Map.of("userId", userId, "online", isOnline));
    }

    // Handle connect event to mark user online
    @org.springframework.context.event.EventListener
    public void handleWebSocketConnectListener(
            org.springframework.web.socket.messaging.SessionConnectedEvent event) {
        java.security.Principal user = event.getUser();
        if (user != null) {
            String userId = user.getName();
            log.info("WebSocket connected: User {}", userId);
            presenceService.markUserOnline(userId);

            // Broadcast online status
            messagingTemplate.convertAndSend(
                    "/topic/status",
                    Map.of("userId", userId, "online", true));
        }
    }

    // Handle disconnect event to mark user offline
    @org.springframework.context.event.EventListener
    public void handleWebSocketDisconnectListener(
            org.springframework.web.socket.messaging.SessionDisconnectEvent event) {
        // We need to retrieve the user ID from the session.
        // In WebSocketConfig, we set the user in the session accessor.
        java.security.Principal user = event.getUser();

        if (user != null) {
            String userId = user.getName();
            log.info("WebSocket disconnect: User {}", userId);

            presenceService.markUserOffline(userId);

            // Broadcast offline status
            messagingTemplate.convertAndSend(
                    "/topic/status",
                    Map.of("userId", userId, "online", false));
        }
    }

    // Handle read receipts
    @MessageMapping("/chat.read")
    public void handleReadReceipt(@Payload Map<String, String> receiptData) {
        String messageId = receiptData.get("messageId");
        String userId = receiptData.get("userId");

        log.info("👁️ Read receipt for message {} by user {}", messageId, userId);

        messageService.markAsRead(messageId);

        // Notify sender about read status
        messagingTemplate.convertAndSend(
                "/topic/receipts",
                Map.of("messageId", messageId, "status", "READ"));
    }

}
