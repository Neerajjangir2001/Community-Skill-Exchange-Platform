package com.reviewService.reviewservice.repository;

import com.reviewService.reviewservice.model.Review;
import com.reviewService.reviewservice.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByTeacherId(UUID teacherId);

    List<Review> findByTeacherIdAndStatus(UUID teacherId, ReviewStatus status);

    List<Review> findByStudentId(UUID studentId);
}
