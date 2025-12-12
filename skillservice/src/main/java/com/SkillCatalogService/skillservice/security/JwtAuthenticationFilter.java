package com.SkillCatalogService.skillservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private Key getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (!"GET".equals(method)) {
            return false;
        }

        // Check public endpoints
        return path.equals("/api/search") ||
                path.equals("/api/search/search") ||
                path.equals("/api/search/skillSearch") ||
                path.matches("/api/search/[^/]+") ||  // /api/skills/{id}
                path.matches("/api/search/user/[^/]+");  // /api/skills/user/{userId}
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug(" Processing request: {} {}", request.getMethod(), request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.debug("JWT token found");

                // Parse claims
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSecretKey())
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                // Extract user ID
                UUID userId = UUID.fromString(claims.getSubject());

                //  FIXED: Handle both "role" (string) and "roles" (array)
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

                log.info(" Valid JWT! UserId: {}, Authorities: {}", userId, authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("Authentication set successfully");
            } else {
                log.debug(" No JWT token found in request");
            }
        } catch (Exception ex) {
            log.error(" JWT validation failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Try "roles" array first (your current token format)
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            for (Object role : rolesList) {
                String roleStr = role.toString();
                // Add ROLE_ prefix if not already present
                if (!roleStr.startsWith("ROLE_")) {
                    roleStr = "ROLE_" + roleStr;
                }
                authorities.add(new SimpleGrantedAuthority(roleStr));
            }
            return authorities;
        }

        // Try "role" single value (alternative format)
        String singleRole = claims.get("role", String.class);
        if (singleRole != null) {
            if (!singleRole.startsWith("ROLE_")) {
                singleRole = "ROLE_" + singleRole;
            }
            authorities.add(new SimpleGrantedAuthority(singleRole));
            return authorities;
        }

        log.warn(" No roles found in token claims");
        return authorities;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }




    }

