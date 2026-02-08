package com.chat_service.service;

import com.chat_service.DTO.ConversationDTO;
import com.chat_service.config.ExternalServiceClient;
import com.chat_service.model.Conversation;
import com.chat_service.repository.ConversationRepository;
import com.chat_service.repository.MessageRepository; // Added import
import com.chat_service.model.MessageStatus; // Added import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository; // Injected
    private final ExternalServiceClient externalServiceClient;
    // Get all conversations for a user

    // Get all conversations for a user
    public List<ConversationDTO> getUserConversations(String userId) {
        log.info(" Fetching conversations for user: {}", userId);

        List<Conversation> conversations = conversationRepository
                .findByParticipantsContainingOrderByLastMessageTimeDesc(userId);

        return conversations.stream()
                .filter(c -> c.getDeletedBy() == null || !c.getDeletedBy().contains(userId)) // Filter deleted
                .map(conversation -> convertToDTO(conversation, userId)) // Pass userId
                .collect(Collectors.toList());
    }

    // Get conversation between two users
    public ConversationDTO getConversation(String user1, String user2) {
        List<Conversation> conversations = conversationRepository.findByParticipants(user1, user2);

        if (!conversations.isEmpty()) {
            Conversation conversation = conversations.get(0);

            // Restore conversation if it was deleted by the user
            if (conversation.getDeletedBy() != null && conversation.getDeletedBy().contains(user1)) {
                conversation.getDeletedBy().remove(user1);
                conversationRepository.save(conversation);
                log.info(" Restored conversation {} for user {}", conversation.getId(), user1);
            }

            return convertToDTO(conversation, user1); // Default to user1 (caller usually)
        }

        // Ensure we don't create duplicates if race condition
        log.info("Creating new conversation between {} and {}", user1, user2);
        Conversation newConversation = Conversation.builder()
                .participants(List.of(user1, user2))
                .lastMessageTime(LocalDateTime.now())
                .unreadCount(0)
                .archived(false)
                .muted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Conversation saved = conversationRepository.save(newConversation);
        return convertToDTO(saved, user1);
    }

    // Archive conversation

    public void archiveConversation(String conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setArchived(true);
            conversationRepository.save(conversation);
            log.info(" Conversation {} archived", conversationId);
        });
    }

    // Mute conversation
    public void muteConversation(String conversationId, boolean muted) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setMuted(muted);
            conversationRepository.save(conversation);
            log.info(" Conversation {} muted: {}", conversationId, muted);
        });
    }

    // Delete conversation (Soft delete for user)
    public void deleteConversation(String conversationId, String userId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            List<String> deletedBy = conversation.getDeletedBy();
            if (deletedBy == null) {
                deletedBy = new java.util.ArrayList<>();
            }
            if (!deletedBy.contains(userId)) {
                deletedBy.add(userId);
                conversation.setDeletedBy(deletedBy);
                conversationRepository.save(conversation);
                log.info(" Conversation {} deleted by user {}", conversationId, userId);
            }
        });
    }

    private ConversationDTO convertToDTO(Conversation conversation, String userId) {
        Map<String, String> names = new HashMap<>();
        Map<String, String> roles = new HashMap<>();
        Map<String, String> locations = new HashMap<>();

        // Fetch names, roles, and locations for participants
        if (conversation.getParticipants() != null) {
            conversation.getParticipants().forEach(participantId -> {
                try {
                    Map<String, Object> userDetails = externalServiceClient.getUserDetails(participantId);
                    String name = (String) userDetails.getOrDefault("name", "User");
                    String role = (String) userDetails.getOrDefault("role", "User");
                    String location = (String) userDetails.getOrDefault("location", "");

                    names.put(participantId, name);
                    roles.put(participantId, role);
                    locations.put(participantId, location);
                } catch (Exception e) {
                    log.warn("Failed to fetch details for user {}", participantId);
                    names.put(participantId, "User");
                    roles.put(participantId, "User");
                    locations.put(participantId, "");
                }
            });
        }

        return ConversationDTO.builder()
                .id(conversation.getId())
                .participants(conversation.getParticipants())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageTime(conversation.getLastMessageTime())
                .unreadCount((int) messageRepository.countByConversationIdAndReceiverIdAndStatusNot(
                        conversation.getId(), userId, MessageStatus.READ)) // Dynamic count: NOT READ
                .archived(conversation.isArchived())
                .muted(conversation.isMuted())
                .participantNames(names)
                .participantRoles(roles)
                .participantLocations(locations)
                .build();
    }
}
