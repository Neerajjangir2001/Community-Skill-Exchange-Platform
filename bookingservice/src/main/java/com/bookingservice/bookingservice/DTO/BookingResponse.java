package com.bookingservice.bookingservice.DTO;

import com.bookingservice.bookingservice.model.Booking;
import com.bookingservice.bookingservice.model.Status;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private UUID userId;
    private UUID providerId;
    private UUID skillId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private BigDecimal totalHours;
    private BigDecimal pricePerHour;
    private BigDecimal totalPrice;
    private Status status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private String userName;
    private String providerName;
    private String skillTitle;
}
