package com.bookingservice.bookingservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingCreatedEvent {

    private UUID bookingId;
    private UUID userId;
    private UUID providerId;
    private UUID skillId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private BigDecimal totalPrice;
    private String status;
    private OffsetDateTime createdAt;
}
