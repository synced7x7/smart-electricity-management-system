package com.desco.authservice.service.impl;

import com.desco.authservice.dto.request.LoginRequest;
import com.desco.authservice.dto.request.RefreshTokenRequest;
import com.desco.authservice.dto.request.RegisterRequest;
import com.desco.authservice.dto.response.AuthResponse;
import com.desco.authservice.entity.User;
import com.desco.authservice.exception.AuthException;
import com.desco.authservice.repository.UserRepository;
import com.desco.authservice.security.JwtService;
import com.desco.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final ApplicationContext    applicationContext;

    // Lazy-fetch to avoid circular dependency
    private AuthenticationManager getAuthenticationManager() {
        return applicationContext.getBean(AuthenticationManager.class);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException(
                "Email address is already registered: " + request.getEmail(),
                HttpStatus.CONFLICT
            );
        }

        User user = User.builder()
            .email(request.getEmail().toLowerCase().trim())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(User.Role.USER)
            .area(request.getArea())
            .isActive(true)
            .build();

        user = userRepository.save(user);
        log.info("User registered successfully — id={} email={}", user.getId(), user.getEmail());

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        getAuthenticationManager().authenticate(
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

    @Override
    public void logout(String bearerToken) {
        log.info("Logout called — token will expire naturally (stateless JWT)");
    }

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
