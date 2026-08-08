package com.desco.authservice.service.impl;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Core authentication business logic.
 *
 * register → validate unique email → persist User → issue tokens
 * login    → authenticate → issue tokens
 * refresh  → validate refresh token → re-issue access token
 * logout   → log (token blocklist can be added here)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Register ──────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());

        // Guard: duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(
                    "Email address is already registered: " + request.getEmail(),
                    HttpStatus.CONFLICT
            );
        }

        // Persist user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)
                .area(request.getArea())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully — id={} email={}", user.getId(), user.getEmail());

        // Issue tokens
        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Delegate credential check to Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new AuthException("Account is deactivated. Contact support.", HttpStatus.FORBIDDEN);
        }

        log.info("Login successful — id={} role={}", user.getId(), user.getRole());
        return buildAuthResponse(user);
    }

    // ── Refresh ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isValid(refreshToken)) {
            throw new AuthException("Refresh token is invalid or has expired", HttpStatus.UNAUTHORIZED);
        }

        UUID userId = jwtService.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.UNAUTHORIZED));

        if (!user.isActive()) {
            throw new AuthException("Account is deactivated", HttpStatus.FORBIDDEN);
        }

        log.info("Token refreshed for userId={}", userId);

        // Issue new access token; keep same refresh token
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .area(user.getArea())
                .build();
    }

    // ── Logout ────────────────────────────────────────────────────

    @Override
    public void logout(String bearerToken) {
        // Stateless JWT: no server-side invalidation needed.
        // Extension point: add token to a Redis blocklist here.
        log.info("Logout called — token will expire naturally (stateless JWT)");
    }

    // ── Private helpers ───────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .area(user.getArea())
                .build();
    }
}
