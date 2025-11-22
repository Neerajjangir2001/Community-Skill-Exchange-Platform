package com.authService.controller;

import com.authService.DTO.jwt.RefreshRequest;
import com.authService.DTO.login.LoginRequest;
import com.authService.DTO.signup.SignupRequest;
import com.authService.repository.RefreshTokenRepository;
import com.authService.service.authService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class authController {

    private final authService authService;

    private final RefreshTokenRepository refreshTokenRepository;


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
       public ResponseEntity<?> logout(@RequestBody RefreshRequest req) {
       authService.logout(req.getRefreshToken());
       return ResponseEntity.ok("Successfully logged out");
    }

    @GetMapping("/{userId}/validate")
    public ResponseEntity<Boolean> validateUser(@PathVariable UUID userId) {
       return ResponseEntity.ok( authService.existByUserId(userId));

    }

}
