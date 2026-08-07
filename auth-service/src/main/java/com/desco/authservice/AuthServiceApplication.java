package com.desco.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DESCO Simulation — Auth Service (port 8081)
 *
 * Responsibilities:
 *  - User registration with bcrypt password hashing
 *  - Login with JWT access + refresh token issuance
 *  - Token refresh
 *  - Logout (token blacklist placeholder)
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
