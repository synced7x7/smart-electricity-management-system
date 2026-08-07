package com.desco.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Slf4j //Automatic Logger
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(
            jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parse(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getSubject(String token) {
        return parse(token).getSubject();
    }

    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    public String getArea(String token) {
        return parse(token).get("area", String.class);
    }

    public String getEmail(String token) {
        return parse(token).get("email", String.class);
    }

    private Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
















/*    Lightweight JWT validator for the user-service.
    In production, the API gateway already validates tokens and
    forwards claims as X-User-* headers. This class is used as a
    fallback for direct-access scenarios (e.g. dev without the gateway).*/
