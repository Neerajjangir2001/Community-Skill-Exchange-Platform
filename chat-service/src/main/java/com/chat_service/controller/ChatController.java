package com.chat_service.controller;

import com.chat_service.DTO.ConversationDTO;
import com.chat_service.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ChatController {

    private final ChatService chatService;


    // Get all conversations for user
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@RequestParam String userId) {
        log.info(" GET /api/chat/conversations - User: {}", userId);
        List<ConversationDTO> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    // Get conversation between two users
    @GetMapping("/conversation")
    public ResponseEntity<ConversationDTO> getConversation(
            @RequestParam String user1,
            @RequestParam String user2) {

        log.info(" GET /api/chat/conversation - Between: {} and {}", user1, user2);
        ConversationDTO conversation = chatService.getConversation(user1, user2);
        return ResponseEntity.ok(conversation);
    }

    // Archive conversation
    @PutMapping("/conversation/{conversationId}/archive")
    public ResponseEntity<Void> archiveConversation(@PathVariable String conversationId) {
        log.info(" PUT /api/chat/conversation/{}/archive", conversationId);
        chatService.archiveConversation(conversationId);
        return ResponseEntity.ok().build();
    }

    // Mute/Unmute conversation
    @PutMapping("/conversation/{conversationId}/mute")
    public ResponseEntity<Void> muteConversation(
            @PathVariable String conversationId,
            @RequestParam boolean muted) {

        log.info(" PUT /api/chat/conversation/{}/mute - Muted: {}", conversationId, muted);
        chatService.muteConversation(conversationId, muted);
        return ResponseEntity.ok().build();
    }

    // Delete conversation
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable String conversationId,
            @RequestParam String userId) {

        log.info(" DELETE /api/chat/conversation/{} - User: {}", conversationId, userId);
        chatService.deleteConversation(conversationId, userId);
        return ResponseEntity.ok().build();
    }

}
