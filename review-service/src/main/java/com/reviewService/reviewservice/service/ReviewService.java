package com.reviewService.reviewservice.service;

import com.reviewService.reviewservice.dto.CreateReviewRequest;
import com.reviewService.reviewservice.dto.ReviewResponse;
import com.reviewService.reviewservice.dto.UserDto;
import com.reviewService.reviewservice.model.Review;
import com.reviewService.reviewservice.model.ReviewStatus;
import com.reviewService.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestTemplate restTemplate;

    public Review createReview(CreateReviewRequest request) {
        Review review = Review.builder()
                .studentId(request.getStudentId())
                .teacherId(request.getTeacherId())
                .rating(request.getRating())
                .comment(request.getComment())
                .status(ReviewStatus.PENDING) // Default to PENDING
                .build();
        return reviewRepository.save(review);
    }

    public List<ReviewResponse> getTeacherReviews(UUID teacherId) {
        List<Review> reviews = reviewRepository.findByTeacherIdAndStatus(teacherId, ReviewStatus.APPROVED);
        return reviews.stream().map(this::mapToReviewResponse).toList();
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        UserDto userDto = null;
        try {
            userDto = restTemplate.getForObject("http://userprofile/api/users/" + review.getStudentId(), UserDto.class);
        } catch (Exception e) {
            // Log error and proceed with null user details
            System.err.println("Failed to fetch user details forreview: " + review.getId());
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .studentId(review.getStudentId())
                .teacherId(review.getTeacherId())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .studentName(userDto != null ? userDto.getDisplayName() : "Anonymous User")
                .studentAvatar(userDto != null ? userDto.getAvatarUrl() : null)
                .build();
    }

    public Double getAverageRating(UUID teacherId) {
        List<Review> reviews = reviewRepository.findByTeacherIdAndStatus(teacherId, ReviewStatus.APPROVED);
        if (reviews.isEmpty())
            return 0.0;

        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        return sum / reviews.size();
    }

    public Review moderateReview(UUID reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(status);
        return reviewRepository.save(review);
    }

    public List<Review> getAllTeacherReviewsIgnoringStatus(UUID teacherId) {
        return reviewRepository.findByTeacherId(teacherId);
    }

    public List<Review> getAllReviews(ReviewStatus status) {
        if (status != null) {
            return reviewRepository.findAll().stream()
                    .filter(r -> r.getStatus() == status)
                    .toList();
        }
        return reviewRepository.findAll();
    }
}
