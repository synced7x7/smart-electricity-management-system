package com.desco.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String area;

    /**
     * Optional. When present and matching the server's configured
     * ADMIN_REGISTRATION_KEY, the account is created with role = ADMIN.
     *
     * Absent or blank means an ordinary customer registration. A present but
     * wrong key is rejected outright rather than quietly downgraded — silently
     * handing someone a USER account when they believe they made an admin is
     * how people end up locked out and confused.
     */
    private String adminKey;
}
