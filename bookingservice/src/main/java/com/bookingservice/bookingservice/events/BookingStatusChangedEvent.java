package com.bookingservice.bookingservice.events;

import com.fasterxml.jackson.annotation.JsonFormat;
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


    private String eventType = "BOOKING_STATUS_CHANGED";

    private UUID bookingId;


    private UUID userId;
    private String userName;
    private String userEmail;
    private UUID providerId;
    private String providerName;
    private String providerEmail;
    private UUID skillId;
    private String skillName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endTime;

    private String sessionTime;


    private String oldStatus;
    private String newStatus;
    private String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime changedAt;
}
