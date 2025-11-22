package com.authService.DTO.signup;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
//    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{8,20}$",
            message = "Password must contain at least 8 characters, one digit, one uppercase, one lowercase, and one special character, Not Empty")
    private String password;
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Na me must be between 3 and 255 characters")
    private String displayName;
    private Set<String> roles;
    private boolean enabled;
//    private LocalDateTime createdDate;
//    private LocalDateTime updatedDate;

}
