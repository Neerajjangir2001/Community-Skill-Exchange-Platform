package com.chat_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    @Indexed
    private String senderId;

    @Indexed
    private String receiverId;

    private String content;

    private MessageType type; // TEXT, IMAGE, FILE, VOICE

    private MessageStatus status; // SENT, DELIVERED, READ

    @CreatedDate
    private LocalDateTime timestamp;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;

    private String fileUrl; // For images/files

    private String fileName;

    private Long fileSize;

    private boolean deleted;

    private String replyToMessageId; // For replies
}
