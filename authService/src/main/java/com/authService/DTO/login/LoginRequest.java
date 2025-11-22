package com.authService.DTO.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest{
    @Email @NotBlank(message = "Email not be empty")
   private String email;
    @NotBlank(message = "Password cannot be empty")
    @NotBlank
    private String password;
}