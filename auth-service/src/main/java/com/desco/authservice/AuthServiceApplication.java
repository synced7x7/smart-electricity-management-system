package com.desco.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// Registration, login, token refresh, logout
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.desco.authservice.repository")
@EntityScan(basePackages = "com.desco.authservice.entity")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
