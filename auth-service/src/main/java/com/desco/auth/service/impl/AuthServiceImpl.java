package com.desco.auth.service.impl;

import com.desco.auth.dto.request.LoginRequest;
import com.desco.auth.dto.request.RefreshTokenRequest;
import com.desco.auth.dto.request.RegisterRequest;
import com.desco.auth.dto.response.AuthResponse;
import com.desco.auth.entity.User;
import com.desco.auth.exception.AdminKeyException;
import com.desco.auth.exception.AuthException;
import com.desco.auth.repository.UserRepository;
import com.desco.auth.security.JwtService;
import com.desco.auth.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${admin.registration-key:}")
    private String adminRegistrationKey;

    /**
     * Refuse to treat a trivially guessable key as valid. A short shared secret
     * on a public endpoint is worse than no feature at all, and this is the one
     * place in the system where a remote caller can obtain ADMIN.
     */
    private static final int MIN_ADMIN_KEY_LENGTH = 16;

    @PostConstruct
    void reportAdminRegistrationPosture() {
        if (adminRegistrationKey == null || adminRegistrationKey.isBlank()) {
            log.info("Admin self-registration DISABLED (ADMIN_REGISTRATION_KEY not set).");
        } else if (adminRegistrationKey.trim().length() < MIN_ADMIN_KEY_LENGTH) {
            // Never log the key itself, only its unsuitability.
            log.warn("ADMIN_REGISTRATION_KEY is shorter than {} characters — admin "
                    + "self-registration is DISABLED until it is replaced with a strong value.",
                    MIN_ADMIN_KEY_LENGTH);
        } else {
            log.info("Admin self-registration ENABLED via ADMIN_REGISTRATION_KEY.");
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(resolveRole(request.getAdminKey()));
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

    /**
     * The only path in the system that can produce an ADMIN account.
     *
     * Rules, in order:
     *   1. No key supplied            -> USER (the overwhelmingly common case).
     *   2. Server has no key, or a weak one -> reject. Never silently grant, and
     *      never silently downgrade.
     *   3. Key supplied and matching  -> ADMIN.
     *
     * The comparison is constant-time. A naive String.equals returns as soon as
     * it hits a differing character, which leaks the length of the matching
     * prefix to anyone able to time the response — enough to recover a shared
     * secret one character at a time.
     */
    private User.UserRole resolveRole(String suppliedKey) {
        if (suppliedKey == null || suppliedKey.isBlank()) {
            return User.UserRole.USER;
        }

        String configured = adminRegistrationKey == null ? "" : adminRegistrationKey.trim();
        if (configured.isEmpty() || configured.length() < MIN_ADMIN_KEY_LENGTH) {
            throw new AdminKeyException(
                    "Administrator registration is not enabled on this server.");
        }

        boolean matches = MessageDigest.isEqual(
                suppliedKey.trim().getBytes(StandardCharsets.UTF_8),
                configured.getBytes(StandardCharsets.UTF_8));

        if (!matches) {
            // Log the attempt, never the value that was tried.
            log.warn("Rejected an administrator registration attempt with an invalid key.");
            throw new AdminKeyException("That administrator key is not valid.");
        }

        log.info("Administrator account created through key-based registration.");
        return User.UserRole.ADMIN;
    }

    private User.AreaName parseArea(String area) {
        if (area == null || area.isBlank()) {
            return null;
        }
        try {
            return User.AreaName.valueOf(area.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Listing all 72 values produced a wall of text no caller could read.
            // Name the rule instead, and offer a few concrete examples.
            throw new IllegalArgumentException("Invalid area '" + area
                    + "'. Must be a Bangladesh district (e.g. DHAKA, CHATTOGRAM, SYLHET, KHULNA) "
                    + "or one of the original DESCO zones (e.g. UTTARA, GULSHAN). "
                    + User.AreaName.values().length + " values are accepted.");
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
