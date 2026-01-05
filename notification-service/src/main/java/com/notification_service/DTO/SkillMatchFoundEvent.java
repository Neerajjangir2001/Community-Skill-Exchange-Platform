package com.notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatchFoundEvent {
    private UUID matchId;
    private UUID teacherId;
    private String teacherName;
    private String teacherEmail;
    private UUID learnerId;
    private String learnerName;
    private String learnerEmail;
    private UUID skillId;
    private String skillName;
    private String skillCategory;
    private LocalDateTime matchedAt;
}