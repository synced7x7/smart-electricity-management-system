package com.desco.authservice.service;

import com.desco.auth.dto.request.LoginRequest;
import com.desco.auth.dto.request.RefreshTokenRequest;
import com.desco.auth.dto.request.RegisterRequest;
import com.desco.auth.dto.response.AuthResponse;

/**
 * Contract for authentication operations.
 */
public interface AuthService {

    /**
     * Register a new USER-role account, create a linked profile row,
     * and return a token pair.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate with email + password and return a token pair.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Exchange a valid refresh token for a new access token.
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Invalidate the current session (no-op for stateless JWT;
     * kept for interface symmetry and future blocklist support).
     */
    void logout(String bearerToken);
}
