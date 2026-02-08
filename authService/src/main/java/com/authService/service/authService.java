package com.authService.service;

import com.authService.DTO.ChangePasswordRequest;
import com.authService.DTO.ForgotPasswordRequest;
import com.authService.DTO.UserResponse;
import com.authService.DTO.jwt.JwtResponse;
import com.authService.DTO.jwt.RefreshRequest;
import com.authService.DTO.login.LoginRequest;
import com.authService.DTO.signup.SignupRequest;
import com.authService.DTO.signup.signupResponse;
import com.authService.exceptionHandler.allExceprionHandles.RefreshTokenErrorHandles;
import com.authService.exceptionHandler.allExceprionHandles.UserAlreadyExistsException;
import com.authService.model.PasswordResetToken;
import com.authService.model.RefreshToken;
import com.authService.model.Role;
import com.authService.model.User;
import com.authService.repository.PasswordResetTokenRepository;
import com.authService.repository.RefreshTokenRepository;
import com.authService.repository.authRepository;
import com.authService.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class authService {

        private final authRepository authRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final RefreshTokenService refreshTokenService;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final KafkaTemplate<String, Object> kafkaTemplate;

        private final AuthenticationManager authenticationManager;

        private final JwtUtil jwtUtil;

        public signupResponse signup(SignupRequest signupRequest) {

                if (authRepository.existsByEmail(signupRequest.getEmail())) {
                        throw new UserAlreadyExistsException("Email already taken");
                }

                Set<Role> roles = Optional.ofNullable(signupRequest.getRoles())
                                .orElse(Set.of("STUDENT"))
                                .stream()
                                .map(Role::valueOf)
                                .collect(Collectors.toSet());

                User newUser = User.builder()
                                .email(signupRequest.getEmail())
                                .password(passwordEncoder.encode(signupRequest.getPassword()))
                                .roles(roles)
                                .enabled(true)
                                .build();

                User save = authRepository.save(newUser);


                return signupResponse.builder()
                                .id(save.getId())
                                .email(save.getEmail())
                                .enabled(save.isEnabled())
                                .createdAt(save.getCreatedAt())
                                .updatedAt(save.getUpdatedAt())
                                .build();

        }

        public JwtResponse login(@Valid LoginRequest loginRequest) {

                Authentication authenticate = authenticationManager
                                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authenticate);

                User user = authRepository.findByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Invalid email: " + loginRequest.getEmail()));

                var rt = refreshTokenService.createRefreshToken(user);
                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(),
                                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));

                return JwtResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(rt.getToken())
                                .expiresInMs(jwtUtil.getAccessTokenExpiryMs())
                                .tokenType("Bearer")
                                .userId(user.getId())
                                .email(user.getEmail())
                                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                                .build();

        }

        public JwtResponse refresh(RefreshRequest req) {
                var byToken = refreshTokenRepository.findByToken(req.getRefreshToken())
                                .orElseThrow(() -> new RefreshTokenErrorHandles("invalid refresh token"));

                if (!byToken.getToken().equals(req.getRefreshToken())) {
                        throw new RefreshTokenErrorHandles("invalid refresh token");
                }

                if (refreshTokenService.isExpired(byToken)) {
                        throw new RefreshTokenErrorHandles("Refresh token expired");

                }

                User user = byToken.getUser();
                refreshTokenService.revoke(byToken);
                var newRt = refreshTokenService.createRefreshToken(user);

                String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(),
                                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));

                return JwtResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(newRt.getToken())
                                .expiresInMs(jwtUtil.getAccessTokenExpiryMs())
                                .userId(user.getId())
                                .email(user.getEmail())
                                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                                .build();
        }

        public void logout(String refreshTokenStr) {
                RefreshToken token = refreshTokenService.findByToken(refreshTokenStr)
                                .orElseThrow(() -> new RefreshTokenErrorHandles("invalid refresh token"));

                if (token.getRevokedAt() != null) {
                        throw new RefreshTokenErrorHandles("invalid refresh token");
                }

                refreshTokenService.revoke(token);

                // Clear the security context
                SecurityContextHolder.clearContext();
        }

        public void logoutAll(User user) {
                refreshTokenService.revokeAllForUser(user);
        }

        public Boolean existByUserId(UUID userId) {
                return authRepository.existsById(userId);
        }

        public String getEmailByUserId(UUID userId) {
                return authRepository.findById(userId)
                                .map(User::getEmail)
                                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        }

        @Transactional
        public void deleteUser(UUID userId) {
                User user = authRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                refreshTokenRepository.deleteAllByUser(user);
                authRepository.delete(user);
        }

        public List<UserResponse> getAllUsers() {
                return authRepository.findAll().stream()
                                .map(user -> UserResponse.builder()
                                                .id(user.getId())
                                                .email(user.getEmail())
                                                .enabled(user.isEnabled())
                                                .roles(user.getRoles().stream().map(Enum::name)
                                                                .collect(Collectors.toSet()))
                                                .build())
                                .collect(Collectors.toList());
        }

        public void changePassword(UUID userId, ChangePasswordRequest request) {
                User user = authRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new RuntimeException("Current password is incorrect");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                authRepository.save(user);
        }

        public void forgotPassword(ForgotPasswordRequest request) {
                User user = authRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found with email: " + request.getEmail()));

                String token = UUID.randomUUID().toString();

                // Remove existing token if any
                passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .token(token)
                                .user(user)
                                .expiryDate(Instant.now().plusSeconds(3600)) // 1 hour
                                .build();

                passwordResetTokenRepository.save(resetToken);

                // Send Kafka event
                // We'll create a dedicated DTO for this event later or use a generic map for
                // now
                // For simplicity, let's assume we send a JSON string or a Map
                // ideally we create PasswordResetEvent DTO in a common lib or duplicate it,
                // let's use a simple Map for now to avoid creating another file if possible, or
                // just a string
                // actually, let's just log it for MVP step 1, then add Kafka properly.
                // Wait, plan says "Implement forgotPassword (Kafka producer)".

                // Let's create a simple event payload
                java.util.Map<String, String> event = java.util.Map.of(
                                "email", user.getEmail(),
                                "token", token,
                                "displayName", user.getEmail() // or fetch name if available
                );

                try {
                        kafkaTemplate.send("user.password.reset", event);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to send password reset event", e);
                }
        }

        public void resetPassword(String token, String newPassword) {
                PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                                .orElseThrow(() -> new RuntimeException("Invalid token"));

                if (resetToken.getExpiryDate().isBefore(java.time.Instant.now())) {
                        throw new RuntimeException("Token expired");
                }

                User user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
                authRepository.save(user);

                passwordResetTokenRepository.delete(resetToken);
        }

}
