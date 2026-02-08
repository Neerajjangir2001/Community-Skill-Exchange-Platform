package com.reviewService.reviewservice.controller;

import com.reviewService.reviewservice.dto.CreateReviewRequest;
import com.reviewService.reviewservice.dto.ReviewResponse;
import com.reviewService.reviewservice.model.Review;
import com.reviewService.reviewservice.model.ReviewStatus;
import com.reviewService.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ReviewResponse>> getTeacherReviews(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(reviewService.getTeacherReviews(teacherId));
    }

    @GetMapping("/teacher/{teacherId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(reviewService.getAverageRating(teacherId));
    }

    // Admin endpoint
    @PutMapping("/{reviewId}/status")
    public ResponseEntity<Review> moderateReview(@PathVariable UUID reviewId, @RequestParam ReviewStatus status) {
        return ResponseEntity.ok(reviewService.moderateReview(reviewId, status));
    }

    // Admin endpoint to get all reviews (optional filter by status)
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(@RequestParam(required = false) ReviewStatus status) {
        return ResponseEntity.ok(reviewService.getAllReviews(status));
    }
}
