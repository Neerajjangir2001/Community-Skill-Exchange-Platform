package com.notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingCreatedEvent {


    // Event metadata
    private String eventType;  // "BOOKING_CREATED"

    // Booking info
    private String bookingId;
    private String userId;      // Student
    private String providerId;  // Teacher
    private String skillId;
    private String startTime;   // ISO format
    private String endTime;
    private BigDecimal totalPrice;
    private String status;
    private OffsetDateTime createdAt;

    // User details (from your event)
    private String userName;      // Student name
    private String userEmail;     // Student email

    // Provider details (from your event)
    private String providerName;  // Teacher name
    private String providerEmail; // Teacher email

    // Skill details
    private String skillName;
    private String sessionTime;   // "04:30 am - 05:30 am"
    private String message;


}
