package com.chat_service.DTO;

import com.chat_service.model.MessageStatus;
import com.chat_service.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String id;
    private String conversationId;
    private String senderId;
    private String receiverId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private LocalDateTime timestamp;
    private String fileUrl;
    private String fileName;
}
