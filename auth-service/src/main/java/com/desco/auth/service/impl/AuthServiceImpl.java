package com.desco.auth.service.impl;

import com.desco.auth.dto.request.LoginRequest;
import com.desco.auth.dto.request.RefreshTokenRequest;
import com.desco.auth.dto.request.RegisterRequest;
import com.desco.auth.dto.response.AuthResponse;
import com.desco.auth.entity.User;
import com.desco.auth.exception.AuthException;
import com.desco.auth.repository.UserRepository;
import com.desco.auth.security.JwtService;
import com.desco.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        user.setArea(parseArea(request.getArea()));

        user = userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiration
        );
    }

    private User.AreaName parseArea(String area) {
        if (area == null || area.isBlank()) {
            return null;
        }
        try {
            return User.AreaName.valueOf(area.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid area '" + area + "'. Allowed values: "
                    + Arrays.toString(User.AreaName.values()));
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new AuthException("User account is inactive");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiration
        );
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new AuthException("Invalid or expired refresh token");
        }

        String email = jwtService.extractEmail(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                request.getRefreshToken(),
                "Bearer",
                accessTokenExpiration
        );
    }
}
