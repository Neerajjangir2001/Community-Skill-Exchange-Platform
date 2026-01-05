package com.authService.controller;

import com.authService.DTO.jwt.RefreshRequest;
import com.authService.DTO.login.LoginRequest;
import com.authService.DTO.signup.SignupRequest;
import com.authService.model.User;
import com.authService.repository.RefreshTokenRepository;
import com.authService.repository.authRepository;
import com.authService.security.JwtUtil;
import com.authService.service.authService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class authController {

    private final authService authService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtil jwtUtil;


    @PostMapping("signup")
    public ResponseEntity<String> userRegister(@Valid @RequestBody SignupRequest signupRequest){
           authService.signup(signupRequest);
        return  ResponseEntity.ok("User Created Successfully");
    }

    @PostMapping("login")
    public ResponseEntity<?> userLogin(@Valid @RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }



    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        var resp = authService.refresh(req);
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshRequest req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        authService.logout(req.getRefreshToken());

        // Manually clear context using SecurityContextLogoutHandler
        new SecurityContextLogoutHandler().logout(request, response, auth);

        return ResponseEntity.ok("Successfully logged out");
    }

    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable UUID userId) {
        boolean exists = authService.existByUserId(userId);
        return ResponseEntity.ok(exists);
    }


    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer "

            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseClaims(token);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "userId", claims.getSubject(),
                        "email", claims.get("email"),
                        "roles", claims.get("roles")
                ));
            }
            return ResponseEntity.status(401).body(Map.of("valid", false));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false, "error", e.getMessage()));
        }
    }



    @GetMapping("/users/{userId}/email")
    public ResponseEntity<String> getUserEmail(@PathVariable UUID userId) {

        String email = authService.getEmailByUserId(userId);
        return ResponseEntity.ok(email);
    }

}
