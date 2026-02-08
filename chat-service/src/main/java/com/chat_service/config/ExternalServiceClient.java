package com.chat_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalServiceClient {

    private final WebClient webClient;

    public Map<String, Object> getUserDetails(String userId) {
        try {
            log.info(" Fetching user details for userId: {}", userId);

            // Call the new endpoint: /api/users/{userId}
            Map response = webClient
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

                // Extract role based on isProvider field
                Boolean isProvider = (Boolean) response.get("isProvider");
                String role = (isProvider != null && isProvider) ? "Mentor" : "Student";
                userDetails.put("role", role);

                // Extract location (city)
                String city = (String) response.get("city");
                userDetails.put("location", city != null ? city : "");

                userDetails.put("id", response.get("userId"));

                log.info(" Fetched user details: name={}, email={}", name, email);
                return userDetails;
            } else {
                log.warn(" User Service returned null for userId: {}", userId);
                return getFallbackUserDetails(UUID.fromString(userId));
            }

        } catch (WebClientResponseException e) {
            log.error(" User Service error for userId: {}. Status: {}, Body: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            return getFallbackUserDetails(UUID.fromString(userId));
        } catch (Exception e) {
            log.error(" Failed to fetch user details for userId: {}. Error: {}",
                    userId, e.getMessage());
            return getFallbackUserDetails(UUID.fromString(userId));
        }
    }

    private Map<String, Object> getFallbackUserDetails(UUID userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("id", userId.toString());
        fallback.put("name", "User " + userId.toString().substring(0, 8));
        fallback.put("email", "user-" + userId.toString().substring(0, 8) + "@example.com");
        fallback.put("role", "User");
        fallback.put("location", "");
        return fallback;
    }
}
