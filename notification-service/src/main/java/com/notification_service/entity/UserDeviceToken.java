package com.notification_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_device_tokens")
public class UserDeviceToken {
    @Id
    private String id;
    @Indexed
    private String userId;
    @Indexed(unique = true)
    private String deviceToken; // FCM token from mobile app
    private String deviceType; // ANDROID, IOS, WEB
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String deviceName;  // "Samsung Galaxy S21", "Chrome on Windows"
    private String osVersion;   // "Android 12", "Windows 10"
    private String appVersion;
    @Indexed
    private boolean active;
    @Indexed
    private LocalDateTime lastUsed;
}
