package com.desco.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// SecurityConfig/GatewayConfig/CorsConfig/JwtAuthFilter/RateLimitFilter/JwtUtil live
// under com.desco.config, com.desco.filter and com.desco.util — siblings of this
// package, not children — so Spring Boot's default scan (this package + children)
// misses all of them. Widen the scan to com.desco so the whole gateway actually loads.
@SpringBootApplication
@ComponentScan(basePackages = "com.desco")
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}








/*
API Gateway
 Single entry-point for all client traffic.
   - JWT validation on every protected route
    - Route forwarding to downstream microservices
  - CORS policy enforcement
   - Rate limiting (per-IP)
 */

