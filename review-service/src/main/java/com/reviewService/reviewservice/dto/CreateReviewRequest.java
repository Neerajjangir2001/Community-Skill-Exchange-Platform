package com.reviewService.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {
    @NotNull
    private UUID teacherId;

    @NotNull
    private UUID studentId;

    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}
