package com.bookingservice.bookingservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDeletedEvent {
    private UUID userId;
    private long timestamp;
}
