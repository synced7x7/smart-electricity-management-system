package com.desco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig { //no need to include cors in other services
    @Bean 
    @Order(Ordered.HIGHEST_PRECEDENCE) //Must run BEFORE Spring Security's own chain.
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:[*]", //supporting any localhost to support clean fallback
                "http://127.0.0.1:[*]", //temporary testing // not needed as of now
                "https://*.vercel.app" // was planned initially. removed due to time constraints
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization", //frontend can send JWT through this
                "Content-Type", //JSOM
                "Accept", // cors
                "Origin", // cors
                "Access-Control-Request-Method", //for cors
                "Access-Control-Request-Headers" //for cors
        ));

        config.setExposedHeaders(List.of( //frontend can read this
                "X-RateLimit-Limit", // removed 
                "X-RateLimit-Remaining", // removed 
                "Authorization"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

