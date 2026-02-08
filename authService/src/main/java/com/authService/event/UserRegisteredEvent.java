package com.authService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEvent {
    private UUID userId;
    private String name;
    private String email;
    private LocalDateTime registeredAt;
}
