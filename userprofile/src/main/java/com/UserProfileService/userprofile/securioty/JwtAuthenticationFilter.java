package com.UserProfileService.userprofile.securioty;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;


import java.security.Key;
import java.util.ArrayList;
import java.util.List;



@Slf4j
@Component
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

        log.debug(" Checking if filter should skip: {} {}", method, path);

        // ✅ Only skip JWT for public GET endpoints
        if (!"GET".equals(method)) {
            log.debug(" Non-GET request requires auth: {} {}", method, path);
            return false;
        }

        // ✅ Fixed regex pattern: single backslash for hyphen
        boolean isPublic =
                path.matches("/api/users/[a-fA-F0-9-]{36}/exists") ||  // UUID with /exists
                        path.matches("/api/users/[a-fA-F0-9-]{36}") ||          // UUID only
                        path.equals("/api/users/search") ||                     // Search endpoint
                        path.startsWith("/actuator/") ||                        // Actuator endpoints
                        path.startsWith("/internal/");                          // Internal endpoints

        if (isPublic) {
            log.debug(" Public endpoint - skipping JWT filter: {} {}", method, path);
        } else {
            log.debug(" Protected endpoint - JWT required: {} {}", method, path);
        }

        return isPublic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("🔐 JWT filter running for: {} {}", request.getMethod(), request.getRequestURI());

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                log.debug("🎫 JWT token found, validating...");

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(getSecretKey())
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String userId = claims.getSubject();
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

                // Create UserDetails object (required by UserSecurityService)
                UserDetails userDetails = User.builder()
                        .username(userId)
                        .password("")
                        .authorities(authorities)
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("✅ JWT authenticated user: {}", userId);

            } else {
                // ❌ No JWT token found - this should NOT happen if shouldNotFilter() works correctly
                log.error("❌ Missing authorization header for: {} {}",
                        request.getMethod(), request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Missing authorization header\"}");
                return;
            }

        } catch (Exception ex) {
            log.error("❌ JWT validation failed: {}", ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List) {
            List<?> rolesList = (List<?>) rolesObj;
            for (Object role : rolesList) {
                String roleStr = role.toString();
                if (!roleStr.startsWith("ROLE_")) {
                    roleStr = "ROLE_" + roleStr;
                }
                authorities.add(new SimpleGrantedAuthority(roleStr));
            }
            return authorities;
        }

        String singleRole = claims.get("role", String.class);
        if (singleRole != null) {
            if (!singleRole.startsWith("ROLE_")) {
                singleRole = "ROLE_" + singleRole;
            }
            authorities.add(new SimpleGrantedAuthority(singleRole));
            return authorities;
        }

        log.warn("⚠️ No roles found in token claims");
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
