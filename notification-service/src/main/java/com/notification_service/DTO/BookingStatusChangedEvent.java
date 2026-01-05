package com.notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatusChangedEvent {


    private UUID bookingId;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private UUID teacherId;
    private String teacherName;
    private String teacherEmail;
    private String skillName;
    private LocalDateTime sessionDate;
    private String sessionTime;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime updatedAt;
}
