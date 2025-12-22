package com.bookingservice.bookingservice.config;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
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
//            return Boolean.TRUE.equals(exists);
        }catch (Exception e) {
            log.error("Failed to validate user {}: {}", userId, e.getMessage());
            return false;
        }
    }



    public SkillDetails getSkill(UUID skillId){
        try {
         return  webClientBuilder
                    .get()
                    .uri("http://SKILLSERVICE/api/search/{id}", skillId)
                    .retrieve()
                    .bodyToMono(SkillDetails.class)
                    .block();
        }catch (Exception e) {
            log.error("Failed to fetch skill {}: {}", skillId, e.getMessage());
            return null;
        }

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
    }
}


