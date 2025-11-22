package com.authService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;


@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpiryMs;


    private Key getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken( UUID userId, String email, Set<String> roles ) {

        Instant now = Instant.now();
        Instant exp = now.plusMillis(accessTokenExpiryMs);


        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("userId", userId.toString())
                .claim("email",email)
                .claim("roles",roles)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();

    }


    public boolean validateToken(String token) {
      try {
          Jwts.parserBuilder()
                  .setSigningKey(getSecretKey())
                  .build()
                  .parseClaimsJws(token);
          return true;
      }catch (Exception e){
          return false;
      }
    }
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessTokenExpiryMs() {
        return this.accessTokenExpiryMs;   // FIXED
    }





}
