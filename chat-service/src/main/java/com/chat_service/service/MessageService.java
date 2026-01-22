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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final NotificationProducer notificationProducer;
//    private final RestTemplate restTemplate;

    private final ExternalServiceClient externalClient;

//    @Value("${user.service.url:http://localhost:8081}")
//    private String userServiceUrl;


    // Send a new message

    @Transactional
    public MessageDTO sendMessage(String senderId, String receiverId, String content, MessageType type) {
        log.info(" Sending message from {} to {}", senderId, receiverId);

        // Get or create conversation
        String conversationId = getOrCreateConversation(senderId,receiverId);


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

    //Get conversation messages with pagination

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

    //Mark message as read
    @Transactional
    public void markAsRead(String conversationId) {
        messageRepository.findById(conversationId).ifPresent(message -> {
            message.setStatus(MessageStatus.READ);
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);
            log.info(" Message {} marked as read", conversationId);
        });
    }

    //Mark all messages in conversation as read

    @Transactional
    public void markConversationAsRead(String conversationId, String userId) {
        List<Message> unreadMessages = messageRepository.findByConversationIdAndStatus(conversationId, MessageStatus.DELIVERED);

        unreadMessages.stream()
                .filter(msg -> msg.getReceiverId().equals(userId))
                .forEach(msg -> {
                    msg.setStatus(MessageStatus.READ);
                    msg.setDeliveredAt(LocalDateTime.now());
                });
        messageRepository.saveAll(unreadMessages);
        log.info(" Marked {} messages as read in conversation {}", unreadMessages.size(), conversationId);
    }

    //Get unread message count for user

    public long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndStatusNot(userId, MessageStatus.READ);
    }

    //Delete message

    @Transactional
    public void deleteMessage(String messageId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setDeleted(true);
            messageRepository.save(message);
            log.info(" Message {} deleted", messageId);
        });
    }


    // ========== PRIVATE HELPER METHODS ==========
    private String getOrCreateConversation(String user1, String user2) {
        return conversationRepository.findByParticipants(user1,user2)
                .map(Conversation::getId)
                .orElseGet(() -> createConversation(user1, user2));
    }

    private String createConversation(String user1, String user2) {
        Conversation conversation = Conversation.builder()
                .participants(List.of(user1, user2))
                .createdAt(LocalDateTime.now())
                .unreadCount(0)
                .archived(false)
                .muted(false)
                .build();

        Conversation saved = conversationRepository.save(conversation);
        log.info("Created new conversation: {}", saved.getId());
        return saved.getId();
    }


    private void updateConversation(String conversationId, String lastMessage) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setLastMessageContent(lastMessage);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);
            conversationRepository.save(conversation);
        });
    }

    private void sendNotification(Message savedMessage) {
        try {
            ChatNotificationEvent event = ChatNotificationEvent.builder()
                    .messageId((savedMessage.getId()))
                    .senderId((savedMessage.getSenderId()))
                    .senderName("User")
                    .receiverId((savedMessage.getReceiverId()))
                    .receiverEmail("receiver@example.com")
                    .messageContent(savedMessage.getContent())
                    .timestamp(savedMessage.getTimestamp())
                    .build();

            notificationProducer.sendMessageNotification(event);
            log.info(" Notification sent for message: {}", savedMessage.getId());
        }catch (Exception e) {
            log.error(" Failed to send notification: {}", e.getMessage());
        }
    }

    //Send notification asynchronously
    private void sendNotificationAsync(Message message) {
        new Thread(() -> {
            try {
                // Get sender info from User Service
                Map<String, Object> senderInfo = externalClient.getUserDetails(message.getSenderId());
                String senderName = senderInfo != null
                        ? (String) senderInfo.get("name")
                        : "Unknown User";

                // Get receiver info from User Service
                Map<String, Object> receiverInfo =externalClient.getUserDetails(message.getReceiverId());
                String receiverEmail = receiverInfo != null
                        ? (String) receiverInfo.get("email")
                        : "unknown@example.com";

                // Create notification event
                ChatNotificationEvent event = ChatNotificationEvent.builder()
                        .messageId((message.getId()))
                        .senderId((message.getSenderId()))
                        .senderName(senderName)
                        .receiverId((message.getReceiverId()))
                        .receiverEmail(receiverEmail)
                        .messageContent(message.getContent())
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

//Get user info from User Service
//    private Map<String, Object> getUserInfo(String userId) {
//        try {
//            String url = externalClient.getUserDetails(userId).toString();
//
//            HttpHeaders headers = new HttpHeaders();
//            HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    entity,
//                    Map.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                return response.getBody();
//            }
//        } catch (Exception e) {
//            log.warn(" Failed to get user info for {}: {}", userId, e.getMessage());
//        }
//        return null;
//    }


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
