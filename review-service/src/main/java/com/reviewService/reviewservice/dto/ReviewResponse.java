package com.reviewService.reviewservice.dto;

import com.reviewService.reviewservice.model.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private UUID id;
    private UUID studentId;
    private UUID teacherId;
    private Integer rating;
    private String comment;
    private ReviewStatus status;
    private Instant createdAt;

    // Enhanced fields
    private String studentName;
    private String studentAvatar;
}
