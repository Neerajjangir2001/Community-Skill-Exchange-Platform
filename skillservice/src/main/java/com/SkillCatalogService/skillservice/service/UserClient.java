package com.SkillCatalogService.skillservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final WebClient.Builder webClientBuilder;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserDetails(UUID userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://USERPROFILE/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("User Service error for userId: {}. Status: {}", userId, e.getStatusCode());
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}. Error: {}", userId, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchUsers(String keyword) {
        try {

            return webClientBuilder.build()
                    .get()
                    .uri("http://USERPROFILE/api/users/search?keyword={keyword}", keyword)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to search users for keyword: {}. Error: {}", keyword, e.getMessage());
            return Collections.emptyList();
        }
    }
}
