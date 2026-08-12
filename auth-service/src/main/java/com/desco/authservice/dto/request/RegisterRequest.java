package com.desco.authservice.dto.request;

import com.desco.authservice.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "New user registration payload")
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(description = "User's full name", example = "Rahim Uddin")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Schema(description = "Unique email address", example = "rahim@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    @Schema(description = "Password (min 8 chars, must include uppercase, lowercase, digit, special char)",
            example = "Secure@1234")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+880|0)[0-9]{10}$", message = "Phone must be a valid Bangladeshi number")
    @Schema(description = "Bangladeshi phone number", example = "+8801712345678")
    private String phone;

    @NotNull(message = "Area is required")
    @Schema(description = "Service area in Dhaka",
            example = "GULSHAN",
            allowableValues = {"UTTARA","GULSHAN","BANANI","DHANMONDI","BASHUNDHARA","MIRPUR","BANASREE","BARIDHARA"})
    private User.Area area;
}
