package com.desco.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//routing requests to the correct microservice
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

    //preventing duplicate cors initialization 
    private static final String DUPLICATED_CORS_HEADERS =
        "Access-Control-Allow-Origin Access-Control-Allow-Credentials";

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(authServiceUrl))

            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(userServiceUrl)) //Example: http://user-service:8082/api/users/profile

            .route("outage-service", r -> r
                .path("/api/outages/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(outageServiceUrl))

            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(notificationServiceUrl))

            .route("complaint-service", r -> r
                .path("/api/complaints/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(complaintServiceUrl))

            .route("payment-service", r -> r
                .path("/api/payments/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(paymentServiceUrl))

            .route("admin-service", r -> r
                .path("/api/admin/**")
                .filters(f -> f.dedupeResponseHeader(DUPLICATED_CORS_HEADERS, "RETAIN_UNIQUE"))
                .uri(adminServiceUrl))

            .build();
    }

    //convert java object to json
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //if we dont use this we get something like 1723456789000
        return mapper;
    }
}
