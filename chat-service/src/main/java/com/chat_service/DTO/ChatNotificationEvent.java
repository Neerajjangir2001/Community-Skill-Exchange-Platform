package com.chat_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatNotificationEvent {

    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverEmail;
    private String messageContent;
    private LocalDateTime timestamp;
}
