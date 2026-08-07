package com.desco.config;

import com.desco.gateway.filter.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route definitions.
 *
 * Routes are also declared in api-gateway.yml (served by config-server).
 * This class registers the JWT filter as a default filter on all routes
 * and provides the ObjectMapper bean used by JwtAuthFilter's error writer.
 *
 * Routes are defined via config-server YAML; this class wires the filter.
 */
@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Value("${AUTH_SERVICE_URL:http://auth-service:8081}")
    private String authServiceUrl;

    @Value("${USER_SERVICE_URL:http://user-service:8082}")
    private String userServiceUrl;

    @Value("${OUTAGE_SERVICE_URL:http://outage-service:8083}")
    private String outageServiceUrl;

    @Value("${NOTIFICATION_SERVICE_URL:http://notification-service:8084}")
    private String notificationServiceUrl;

    @Value("${COMPLAINT_SERVICE_URL:http://complaint-service:8085}")
    private String complaintServiceUrl;

    @Value("${PAYMENT_SERVICE_URL:http://payment-service:8086}")
    private String paymentServiceUrl;

    @Value("${ADMIN_SERVICE_URL:http://admin-service:8087}")
    private String adminServiceUrl;

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Programmatic route definitions with JWT filter applied to every route.
     * These supplement (and override if conflicting) the YAML routes.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            // ── Auth Service (public + private) ──────────────────
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(authServiceUrl))

            // ── User Service ──────────────────────────────────────
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(userServiceUrl))

            // ── Outage Service ────────────────────────────────────
            .route("outage-service", r -> r
                .path("/api/outages/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(outageServiceUrl))

            // ── Notification Service ──────────────────────────────
            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(notificationServiceUrl))

            // ── Complaint Service ─────────────────────────────────
            .route("complaint-service", r -> r
                .path("/api/complaints/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(complaintServiceUrl))

            // ── Payment Service ───────────────────────────────────
            .route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(paymentServiceUrl))

            // ── Admin Service ─────────────────────────────────────
            .route("admin-service", r -> r
                .path("/api/admin/**")
                .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthFilter.Config())))
                .uri(adminServiceUrl))

            .build();
    }

    /**
     * Shared ObjectMapper with Java 8 time support.
     * Used by JwtAuthFilter's JSON error response writer.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
