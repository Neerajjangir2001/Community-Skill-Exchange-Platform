package com.authService.service;

import com.authService.model.RefreshToken;
import com.authService.model.User;
import com.authService.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final Environment env;

    public RefreshToken createRefreshToken(User user){
        long ttl = Long.parseLong(env.getProperty("app.jwt.refresh-token-expiration-ms", "2592000000"));
        Instant now = Instant.now();
        RefreshToken refreshToken =  RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .createdAt(now)
                .expiresAt(now.plusMillis(ttl))
                .build();
      return   refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isExpired(RefreshToken token){
        return token.getExpiresAt().isBefore(Instant.now()) || token.getRevokedAt() != null;

     }

    public void revoke(RefreshToken token) {
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);
    }

    public void revokeAllForUser(User user) {
        refreshTokenRepository.findAllByUser(user).forEach(t -> {
            t.setRevokedAt(Instant.now());
            refreshTokenRepository.save(t);
        });

    }

}
