package com.desco.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
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

