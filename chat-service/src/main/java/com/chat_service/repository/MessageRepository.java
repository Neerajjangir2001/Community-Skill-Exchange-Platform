package com.chat_service.repository;

import com.chat_service.model.Message;
import com.chat_service.model.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {

    // Find messages in a conversation
    Page<Message> findByConversationIdOrderByTimestampDesc(String conversationId, Pageable pageable);

    // Find messages between two users
    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampDesc(
            String senderId1, String receiverId1, String senderId2, String receiverId2);

    // Count unread messages
    long countByReceiverIdAndStatusNot(String receiverId, MessageStatus status);

    // Find unread messages
    List<Message> findByReceiverIdAndStatus(String receiverId, MessageStatus status);

    // Find messages by conversation and status
    List<Message> findByConversationIdAndStatus(String conversationId, MessageStatus status);

    // Find messages after a timestamp
    List<Message> findByConversationIdAndTimestampAfter(String conversationId, LocalDateTime timestamp);
}
