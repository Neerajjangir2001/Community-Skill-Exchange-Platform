package com.SkillCatalogService.skillservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class AuthClient {
    private final WebClient webClient;


    public Boolean validateUser(UUID userId) {
        try {
            return   webClient.get().uri("/auth/exists/{userId}", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        }catch (WebClientResponseException e){
            e.printStackTrace();
        }
        return false;
    }
}
