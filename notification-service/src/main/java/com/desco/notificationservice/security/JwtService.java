package com.desco.notificationservice.security;

import com.desco.notificationservice.enums.Area;
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
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtService initialised for notification-service");
    }

    public boolean isValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration() != null && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Area extractArea(String token) {
        String areaStr = extractAllClaims(token).get("area", String.class);
        if (areaStr == null || areaStr.isBlank()) {
            return null;
        }
        try {
            return Area.valueOf(areaStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown area claim in JWT: {}", areaStr);
            return null;
        }
    }

    public UserPrincipal extractPrincipal(String token) {
        Claims claims = extractAllClaims(token);
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        String areaStr = claims.get("area", String.class);
        Area area = null;
        if (areaStr != null && !areaStr.isBlank()) {
            try {
                area = Area.valueOf(areaStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return UserPrincipal.builder()
                .id(userId)
                .email(email)
                .role(role)
                .area(area)
                .build();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
