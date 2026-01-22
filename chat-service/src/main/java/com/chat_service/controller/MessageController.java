package com.chat_service.controller;

import com.chat_service.DTO.MessageDTO;
import com.chat_service.model.MessageType;
import com.chat_service.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class MessageController {
    private final MessageService messageService;


    //Send a new message
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody Map<String, String> request) {
        String authenticatedUserId = getCurrentUserId();
        String receiverId = request.get("receiverId");
        String content = request.get("content");
        String type = request.getOrDefault("type", "TEXT");

        log.info(" Authenticated user {} sending message to {}", authenticatedUserId, receiverId);

        MessageDTO message = messageService.sendMessage(
                authenticatedUserId,
                receiverId,
                content,
                MessageType.valueOf(type)
        );

        return ResponseEntity.ok(message);
    }


    //Get conversation messages
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<MessageDTO>> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        String userId = getCurrentUserId();
        log.info(" User {} fetching conversation: {}", userId, conversationId);

        Page<MessageDTO> messages = messageService.getConversationMessages(conversationId, page, size);
        return ResponseEntity.ok(messages);
    }

    //Mark message as delivered
    @PutMapping("/{messageId}/delivered")
    public ResponseEntity<Void> markAsDelivered(@PathVariable String messageId) {
        log.info(" PUT /api/messages/{}/delivered", messageId);
        messageService.markAsDelivered(messageId);
        return ResponseEntity.ok().build();
    }

    //Mark message as read
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String messageId) {
        String userId = getCurrentUserId();
        log.info("️ User {} marking message {} as read", userId, messageId);

        messageService.markAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    //Mark all messages in conversation as read
    @PutMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable String conversationId,
            @RequestParam String userId) {

        log.info(" PUT /api/messages/conversation/{}/read - User: {}", conversationId, userId);
        messageService.markConversationAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    //Get unread message count
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String userId = getCurrentUserId();
        log.info(" Getting unread count for user: {}", userId);

        long count = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    //Delete message
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        String userId = getCurrentUserId();
        log.info(" User {} deleting message {}", userId, messageId);

        // Add check: only allow deletion if user is the sender
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok().build();
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "chat-service",
                "port", "8089"
        ));
    }


    //Get current authenticated user ID
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

}
