package com.authService.security;

import com.authService.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        String refreshToken = request.getHeader("Refresh-Token"); // or extract from request

        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken)
                    .ifPresent(refreshTokenService::revoke);
        }

        SecurityContextHolder.clearContext();
    }
}
