package com.desco.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PingController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("service", "auth-service", "status", "ok");
    }
}
