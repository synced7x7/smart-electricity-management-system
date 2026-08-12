package com.desco.authservice.service;

import com.desco.authservice.dto.request.LoginRequest;
import com.desco.authservice.dto.request.RefreshTokenRequest;
import com.desco.authservice.dto.request.RegisterRequest;
import com.desco.authservice.dto.response.AuthResponse;


public interface AuthService {

     //Register a new USER-role account
    AuthResponse register(RegisterRequest request);

    //Authenticate with email + password
    AuthResponse login(LoginRequest request);

    //Exchange a valid refresh token for a new access token.
    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String bearerToken);
}
