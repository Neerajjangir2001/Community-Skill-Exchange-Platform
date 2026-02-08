package com.UserProfileService.userprofile.service;

import jakarta.inject.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthClient {
    private final WebClient webClient;

    public Boolean validateUser(UUID userId) {
        try {
            return webClient.get().uri("/auth/exists/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (WebClientResponseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserEmail(UUID userId) {
        try {
            log.debug("Fetching email for userId: {}", userId);

            // Fetch user details from Auth Service
            String email = webClient
                    .get()
                    .uri("/auth/users/{userId}/email", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (email != null && !email.isEmpty()) {
                log.info(" Found email for userId {}: {}", userId, email);
                return email;
            } else {
                log.warn("️ Empty email returned for userId: {}", userId);
                return generateFallbackEmail(userId);
            }

        } catch (WebClientResponseException e) {
            log.error(" HTTP Error fetching email for userId: {}. Status: {}, Body: {}",
                    userId, e.getStatusCode(), e.getResponseBodyAsString());
            return generateFallbackEmail(userId);

        } catch (Exception e) {
            log.error(" Unexpected error fetching email for userId: {}", userId, e);
            return generateFallbackEmail(userId);
        }
    }

    private String generateFallbackEmail(UUID userId) {
        return "user-" + userId.toString().substring(0, 8) + "@example.com";
    }

    public void deleteUser(UUID userId) {
        try {
            webClient.delete()
                    .uri("/auth/users/{userId}", userId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Deleted user {} from Auth Service", userId);
        } catch (WebClientResponseException e) {
            log.error("Failed to delete user from Auth Service. Status: {}", e.getStatusCode());
            throw new RuntimeException("Failed to delete user credentials", e);
        } catch (Exception e) {
            log.error("Unexpected error deleting user from Auth Service", e);
            throw new RuntimeException("Failed to delete user credentials", e);
        }
    }
}
