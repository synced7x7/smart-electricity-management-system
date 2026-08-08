package com.desco.authservice.controller;

import com.desco.auth.dto.request.LoginRequest;
import com.desco.auth.dto.request.RefreshTokenRequest;
import com.desco.auth.dto.request.RegisterRequest;
import com.desco.auth.dto.response.ApiResponse;
import com.desco.auth.dto.response.AuthResponse;
import com.desco.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints:
 *   POST /api/auth/register  — public
 *   POST /api/auth/login     — public
 *   POST /api/auth/refresh   — public (token required)
 *   POST /api/auth/logout    — protected
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, refresh token and logout")
public class AuthController {

    private final AuthService authService;

    // ── Register ──────────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Registration successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/auth/register — email={}", request.getEmail());
        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    // ── Login ─────────────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT tokens")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/auth/login — email={}", request.getEmail());
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ── Refresh ───────────────────────────────────────────────────

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a refresh token for a new access token")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Token refreshed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Refresh token invalid or expired")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("POST /api/auth/refresh");
        AuthResponse response = authService.refresh(request);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    // ── Logout ────────────────────────────────────────────────────

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout (invalidates client-side token)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        log.info("POST /api/auth/logout");
        authService.logout(authHeader);

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
