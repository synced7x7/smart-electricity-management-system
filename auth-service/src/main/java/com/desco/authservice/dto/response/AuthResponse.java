package com.desco.authservice.dto.response;

import com.desco.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Returned on successful login or token refresh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "JWT token pair and authenticated user summary")
public class AuthResponse {

    @Schema(description = "Short-lived access token (24 h)", example = "eyJhbGci...")
    private String accessToken;

    @Schema(description = "Long-lived refresh token (7 days)", example = "eyJhbGci...")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in milliseconds", example = "86400000")
    private long expiresIn;

    // ── Authenticated user summary ────────────────────────────────

    @Schema(description = "User UUID")
    private UUID userId;

    @Schema(description = "User email", example = "rahim@example.com")
    private String email;

    @Schema(description = "Assigned role", example = "USER")
    private User.Role role;

    @Schema(description = "Service area", example = "GULSHAN")
    private User.Area area;
}
