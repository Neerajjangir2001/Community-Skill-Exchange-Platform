package com.authService.service;


import com.authService.DTO.jwt.JwtResponse;
import com.authService.DTO.jwt.RefreshRequest;
import com.authService.DTO.login.LoginRequest;
import com.authService.DTO.signup.SignupRequest;
import com.authService.DTO.signup.signupResponse;
import com.authService.exceptionHandler.allExceprionHandles.PasswordHandleException;
import com.authService.exceptionHandler.allExceprionHandles.RefreshTokenErrorHandles;
import com.authService.exceptionHandler.allExceprionHandles.RegisterSuccess;
import com.authService.exceptionHandler.allExceprionHandles.UserAlreadyExistsException;
import com.authService.model.RefreshToken;
import com.authService.model.Role;
import com.authService.model.User;
import com.authService.repository.RefreshTokenRepository;
import com.authService.repository.authRepository;
import com.authService.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                .displayName(signupRequest.getDisplayName())
                .roles(roles)
                .enabled(true)
                .build();

        User save = authRepository.save(newUser);

//        var refreshToken = refreshTokenService.createRefreshToken(save);
//
//                jwtUtil.generateAccessToken(save.getId(),save.getEmail(),save.getRoles()
//                        .stream().map(Enum::name).collect(Collectors.toSet())
//                );

        return  signupResponse.builder()
                .id(save.getId())
                .email(save.getEmail())
                .enabled(save.isEnabled())
                .createdAt(save.getCreatedAt())
                .updatedAt(save.getUpdatedAt())
                .displayName(save.getDisplayName())
                .build();







    }

    public JwtResponse login(@Valid LoginRequest loginRequest) {

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

//        User user = authRepository.findByEmail(loginRequest.getEmail())
//             .orElseThrow(() -> new UsernameNotFoundException("invalid this email: " + loginRequest.getEmail() + " Create new account"));

//        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
//            throw new PasswordHandleException("invalid password");
//        }

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        User user = authRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Invalid email: " + loginRequest.getEmail()
                ));



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

    public JwtResponse refresh(RefreshRequest req){
       var  byToken = refreshTokenRepository.findByToken(req.getRefreshToken())
               .orElseThrow(() -> new RefreshTokenErrorHandles("invalid refresh token"));

       if (!byToken.getToken().equals(req.getRefreshToken())) {
           throw new RefreshTokenErrorHandles("invalid refresh token");
       }

       if (refreshTokenService.isExpired(byToken)){
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


        refreshTokenService.findByToken(refreshTokenStr)
                .map(token -> {
                    refreshTokenService.revoke(token);
                    return  token;
                })
                .orElseThrow(() -> new RefreshTokenErrorHandles("invalid refresh token"));


//                .ifPresent(refreshTokenService::revoke);
    }

    public void logoutAll(User user) {
        refreshTokenService.revokeAllForUser(user);
    }

    public Boolean existByUserId(UUID userId) {
        return authRepository.existsById(userId);
    }
}
