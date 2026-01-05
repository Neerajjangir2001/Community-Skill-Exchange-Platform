package com.notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceivedEvent {
    private UUID messageId;
    private UUID senderId;
    private String senderName;
    private UUID receiverId;
    private String receiverEmail;
    private String messageContent;
    private LocalDateTime sentAt;
}