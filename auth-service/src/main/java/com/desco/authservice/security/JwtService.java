package com.desco.authservice.security;

import com.desco.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles JWT access-token and refresh-token lifecycle.
 *
 * Access token  — short-lived (default 24 h), carries role + area claims.
 * Refresh token — long-lived  (default 7 d),  carries only subject.
 */
@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
        log.info("JwtService initialised — access TTL={}ms refresh TTL={}ms",
                accessTokenExpirationMs, refreshTokenExpirationMs);
    }

    // ── Token generation ──────────────────────────────────────────

    /**
     * Build a signed access token embedding user identity claims.
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role",  user.getRole().name());
        if (user.getArea() != null) {
            claims.put("area", user.getArea().name());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Build a signed refresh token (subject only, no role/area).
     */
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    // ── Token validation ──────────────────────────────────────────

    public boolean isValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ── Claim extraction ──────────────────────────────────────────

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    // ── Private helpers ───────────────────────────────────────────

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
