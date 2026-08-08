package com.desco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

//allows which frontend can communicate with backend
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173",    // Vite dev server
                "https://*.vercel.app"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization", //frontend can send JWT through this
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        config.setExposedHeaders(List.of( //frontend can read this
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "Authorization"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}















/**
 * Global CORS policy applied at the gateway level.
 *
 * Allowed origins:
 *  - http://localhost:3000       (local dev)
 *  - https://*.vercel.app        (Vercel preview/prod deployments)
 *
 * Downstream services do NOT need their own CORS config because
 * they are only reachable via the gateway in production.
 */
