package com.chat_service.service;

import com.chat_service.DTO.ChatNotificationEvent;
import com.chat_service.DTO.MessageDTO;
import com.chat_service.config.ExternalServiceClient;
import com.chat_service.model.Conversation;
import com.chat_service.model.Message;
import com.chat_service.model.MessageStatus;
import com.chat_service.model.MessageType;
import com.chat_service.repository.ConversationRepository;
import com.chat_service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationProducer notificationProducer;
    private final ExternalServiceClient externalClient;

    // Send a new message

    @Transactional
    public MessageDTO sendMessage(String senderId, String receiverId, String content, MessageType type) {
        log.info(" Sending message from {} to {}", senderId, receiverId);

        // Get or create conversation
        String conversationId = getOrCreateConversation(senderId, receiverId);

        // Create message
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .type(type)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .deleted(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info(" Message saved with ID: {}", savedMessage.getId());

        // Update conversation
        updateConversation(conversationId, content);

        // Send notification via Kafka
        sendNotificationAsync(savedMessage);

        return convertToDTO(savedMessage);
    }

    // Get conversation messages with pagination

    public Page<MessageDTO> getConversationMessages(String conversationId, int page, int size) {
        log.info(" Fetching messages for conversation: {}", conversationId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByConversationIdOrderByTimestampDesc(
                conversationId, pageable);

        return messages.map(this::convertToDTO);
    }

    // Mark message as delivered
    @Transactional
    public void markAsDelivered(String conversationId) {
        messageRepository.findById(conversationId).ifPresent(message -> {
            message.setStatus(MessageStatus.DELIVERED);
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);
            log.info(" Message {} marked as delivered", conversationId);
        });
    }

    // Mark message as read
    @Transactional
    public void markAsRead(String conversationId) {
        messageRepository.findById(conversationId).ifPresent(message -> {
            message.setStatus(MessageStatus.READ);
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);
            log.info(" Message {} marked as read", conversationId);
            sendStatusUpdateNotificationAsync(message, MessageStatus.READ);
        });
    }

    // Mark all messages in conversation as read

    @Transactional
    public void markConversationAsRead(String conversationId, String userId) {
        List<Message> unreadMessages = messageRepository.findByConversationIdAndStatusNot(conversationId,
                MessageStatus.READ);

        unreadMessages.stream()
                .filter(msg -> msg.getReceiverId().equals(userId))
                .forEach(msg -> {
                    msg.setStatus(MessageStatus.READ);
                    msg.setDeliveredAt(LocalDateTime.now());
                    // Notify sender for each message (or batch? individual for now to be simple)
                    sendStatusUpdateNotificationAsync(msg, MessageStatus.READ);
                });
        messageRepository.saveAll(unreadMessages);
        log.info(" Marked {} messages as read in conversation {}", unreadMessages.size(), conversationId);
    }

    // Get unread message count
    public long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndStatusNot(userId, MessageStatus.READ);
    }

    // Delete message
    @Transactional
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
        log.info(" Message deleted with ID: {}", messageId);
    }

    // ========== PRIVATE HELPER METHODS ==========

    // Send status update notification (to the SENDER of the message)
    private void sendStatusUpdateNotificationAsync(Message message, MessageStatus status) {
        new Thread(() -> {
            try {
                // Determine who to notify. content is status update.
                // We want to notify the original SENDER that their message was READ.
                String targetUserId = message.getSenderId();

                // Create notification event
                ChatNotificationEvent event = ChatNotificationEvent.builder()
                        .messageId(message.getId())
                        .conversationId(message.getConversationId())
                        .senderId(message.getSenderId()) // Original sender
                        .receiverId(targetUserId) // Notification goes to original sender
                        .messageContent(null) // Content null for status update? Or "Read"
                        .status(status)
                        .timestamp(LocalDateTime.now())
                        .build();

                // We need a way to route this specific notification to the specific user.
                // sending to /user/{targetUserId}/topic/notifications requires the event to
                // have receiverId = targetUserId
                // My NotificationService probably uses event.getReceiverId() to route.
                // So setting receiverId = targetUserId is correct.

                notificationProducer.sendMessageNotification(event);
                log.info(" Status update notification ({}) sent to user: {}", status, targetUserId);

            } catch (Exception e) {
                log.error(" Failed to send status notification: {}", e.getMessage(), e);
            }
        }).start();
    }

    // Send notification asynchronously
    private void sendNotificationAsync(Message message) {
        new Thread(() -> {
            try {
                // Get sender info from User Service
                Map<String, Object> senderInfo = externalClient.getUserDetails(message.getSenderId());
                String senderName = senderInfo != null
                        ? (String) senderInfo.get("name")
                        : "Unknown User";

                // Get receiver info from User Service
                Map<String, Object> receiverInfo = externalClient.getUserDetails(message.getReceiverId());
                String receiverEmail = receiverInfo != null
                        ? (String) receiverInfo.get("email")
                        : "unknown@example.com";

                // Create notification event
                ChatNotificationEvent event = ChatNotificationEvent.builder()
                        .messageId((message.getId()))
                        .conversationId(message.getConversationId())
                        .senderId((message.getSenderId()))
                        .senderName(senderName)
                        .receiverId((message.getReceiverId()))
                        .receiverEmail(receiverEmail)
                        .messageContent(message.getContent())
                        .status(message.getStatus())
                        .timestamp(message.getTimestamp())
                        .build();

                // Send to Kafka
                notificationProducer.sendMessageNotification(event);
                log.info(" Notification sent to Kafka for message: {}", message.getId());

            } catch (Exception e) {
                log.error(" Failed to send notification: {}", e.getMessage(), e);
            }
        }).start();
    }

    // Get or create conversation between two users
    private String getOrCreateConversation(String senderId, String receiverId) {
        List<Conversation> conversations = conversationRepository.findByParticipants(senderId, receiverId);

        if (!conversations.isEmpty()) {
            return conversations.get(0).getId();
        }

        Conversation newConversation = Conversation.builder()
                .participants(List.of(senderId, receiverId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .unreadCount(0)
                .archived(false)
                .muted(false)
                .build();

        return conversationRepository.save(newConversation).getId();
    }

    // Update conversation with last message details
    private void updateConversation(String conversationId, String content) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setLastMessageContent(content);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUpdatedAt(LocalDateTime.now());

            // Un-delete for everyone since a new message arrived
            if (conversation.getDeletedBy() != null) {
                conversation.getDeletedBy().clear();
            }

            conversationRepository.save(conversation);
        });
    }

    private MessageDTO convertToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .timestamp(message.getTimestamp())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .build();
    }

}
