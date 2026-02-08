package com.chat_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Allow WebSocket and health endpoints without authentication
        return path.startsWith("/ws") ||
                path.equals("/api/messages/health") ||
                path.equals("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.info("Processing request: {} {}", request.getMethod(), request.getServletPath());

            // 1. Trust Gateway Authentication
            String gatewayUserId = request.getHeader("X-User-Id");
            log.info("X-User-Id header: {}", gatewayUserId);

            if (gatewayUserId != null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        gatewayUserId,
                        null,
                        new ArrayList<>());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Fallback: Direct JWT Validation
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                String userId = jwtUtil.extractUserId(jwt);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(jwt)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                new ArrayList<>());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug(" JWT authenticated user: {}", userId);
                    }
                }
            }
        } catch (Exception e) {
            log.error(" JWT authentication error: {}", e.getMessage());
            // Important: Do NOT proceed down chain if auth failed exception occurs?
            // Actually, if auth fails, we usually just continue anonymously or let filter
            // chain handle 403.
            // But here we just log and continue.
        }

        filterChain.doFilter(request, response);
    }
}
