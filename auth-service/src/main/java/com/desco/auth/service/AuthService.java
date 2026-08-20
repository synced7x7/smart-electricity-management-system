package com.desco.auth.service;

import com.desco.auth.dto.request.LoginRequest;
import com.desco.auth.dto.request.RefreshTokenRequest;
import com.desco.auth.dto.request.RegisterRequest;
import com.desco.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
