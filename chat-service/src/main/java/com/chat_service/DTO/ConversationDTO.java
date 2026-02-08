package com.chat_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String id;
    private List<String> participants;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private int unreadCount;
    private boolean archived;
    private boolean muted;
    private java.util.Map<String, String> participantNames; // ID -> Name mapping
    private java.util.Map<String, String> participantRoles; // ID -> Role mapping (e.g., "Student", "Mentor")
    private java.util.Map<String, String> participantLocations; // ID -> Location mapping (e.g., "New York, USA")
}
