package com.notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreatedEvent {
    private UUID reviewId;
    private UUID bookingId;
    private UUID reviewerId;
    private String reviewerName;
    private UUID teacherId;
    private String teacherName;
    private String teacherEmail;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}