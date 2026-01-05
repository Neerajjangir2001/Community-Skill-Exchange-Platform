package com.bookingservice.bookingservice.config;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalServiceClient {
    private final WebClient webClientBuilder;

    public boolean validateUser(UUID userId) {
        try {
            return Boolean.TRUE.equals(webClientBuilder
                    .get()
                    .uri("/api/users/{id}/exists", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block());
        } catch (Exception e) {
            log.error("Failed to validate user {}: {}", userId, e.getMessage());
            return false;
        }
    }




    public SkillDetails getSkill(UUID skillId) {
        try {
            return webClientBuilder
                    .get()
                    .uri("http://SKILLSERVICE/api/search/{id}", skillId)
                    .retrieve()
                    .bodyToMono(SkillDetails.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch skill {}: {}", skillId, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getUserDetails(UUID userId) {
        try {
            log.info("🔍 Fetching user details for userId: {}", userId);

            // Call the new endpoint: /api/users/{userId}
            Map<String, Object> response = webClientBuilder
                    .get()
                    .uri("http://USERPROFILE/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                Map<String, Object> userDetails = new HashMap<>();

                // Extract name from "displayName" field
                String name = (String) response.get("displayName");
                userDetails.put("name", name != null ? name : "Unknown User");

                // Extract email - check if your ProfileDto has email field
                String email = (String) response.get("email");
                if (email == null) {
                    // Fallback: construct email from userId if not available
                    email = "user-" + userId.toString().substring(0, 8) + "@example.com";
                }
                userDetails.put("email", email);

                userDetails.put("id", response.get("userId"));

                log.info("✅ Fetched user details: name={}, email={}", name, email);
                return userDetails;
            } else {
                log.warn("⚠️ User Service returned null for userId: {}", userId);
                return getFallbackUserDetails(userId);
            }

        } catch (WebClientResponseException e) {
            log.error(" User Service error for userId: {}. Status: {}, Body: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            return getFallbackUserDetails(userId);
        } catch (Exception e) {
            log.error(" Failed to fetch user details for userId: {}. Error: {}",
                    userId, e.getMessage());
            return getFallbackUserDetails(userId);
        }
    }

    private Map<String, Object> getFallbackUserDetails(UUID userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", userId.toString());
        fallback.put("name", "User " + userId.toString().substring(0, 8));
        fallback.put("email", "user-" + userId.toString().substring(0, 8) + "@example.com");
        return fallback;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDetails {
        private UUID id;
        private UUID userId;
        private String title;
        private String description;
        private BigDecimal pricePerHour;
        private String status;

        public String getName() {
            return this.title;
        }
    }
}


