package com.bookingservice.bookingservice.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingStatusChangeRequest {

    @NotBlank(message = "Action is required")
    private String action; // ACCEPT, REJECT, CANCEL, COMPLETE

    private String reason;
}
