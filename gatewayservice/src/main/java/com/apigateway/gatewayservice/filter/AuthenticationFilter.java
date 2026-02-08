package com.apigateway.gatewayservice.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${app.jwt.secret}")
    private String secretKey;

    // ADDED: ObjectMapper for safe type conversion
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationFilter() {
        super(Config.class);
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            // Bypass auth for WebSocket handshake - Check for any path containing /chat/ws
            // This covers /api/chat/ws, /chat/ws, etc.
            if (exchange.getRequest().getURI().getPath().contains("/chat/ws")) {
                return chain.filter(exchange);
            }

            // Check if Authorization header exists
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Extract token
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            } else {
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(authHeader)
                        .getBody();

                // Extract userId and email
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);

                // FIX: Handle roles as List and convert to comma-separated String
                String rolesString = "";
                Object rolesObj = claims.get("roles");

                if (rolesObj != null) {
                    try {
                        // Try to convert to List<String>
                        List<String> rolesList = objectMapper.convertValue(
                                rolesObj,
                                new TypeReference<List<String>>() {
                                });
                        rolesString = String.join(",", rolesList);
                    } catch (Exception e) {
                        // If it's already a String, use it directly
                        if (rolesObj instanceof String) {
                            rolesString = (String) rolesObj;
                        }
                    }
                }

                // FIXED: Properly mutate the request and forward it
                String finalRolesString = rolesString;
                ServerWebExchange mutatedExchange = exchange.mutate()
                        .request(builder -> builder
                                .header("X-User-Id", userId)
                                .header("X-User-Email", email)
                                .header("X-User-Roles", finalRolesString))
                        .build();

                return chain.filter(mutatedExchange);

            } catch (Exception e) {
                System.err.println("JWT validation failed: " + e.getMessage());
                e.printStackTrace();
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        System.err.println("Authentication Error: " + err);
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
