package com.chat_service.service;

import com.chat_service.DTO.ConversationDTO;
import com.chat_service.model.Conversation;
import com.chat_service.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private ConversationRepository conversationRepository;

    //Get all conversations for a user

    public List<ConversationDTO> getUserConversations(String userId) {
        log.info(" Fetching conversations for user: {}", userId);

        List<Conversation> conversations = conversationRepository.findByParticipantsContainingOrderByLastMessageTimeDesc(userId);

      return   conversations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

// Get conversation between two users
    public ConversationDTO getConversation(String user1, String user2) {
        return conversationRepository.findByParticipants(user1, user2)
                .map(this::convertToDTO)
                .orElse(null);
    }

    //Archive conversation

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


    private ConversationDTO convertToDTO(Conversation conversation) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .participants(conversation.getParticipants())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageTime(conversation.getLastMessageTime())
                .unreadCount(conversation.getUnreadCount())
                .archived(conversation.isArchived())
                .muted(conversation.isMuted())
                .build();
    }
}
