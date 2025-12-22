package com.bookingservice.bookingservice.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusChangedEvent {
    private UUID bookingId;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private OffsetDateTime changedAt;
}
