package com.desco.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

//allows which frontend can communicate with backend
@Configuration
public class CorsConfig {

    /**
     * Must run BEFORE Spring Security's own chain.
     *
     * A plain @Bean WebFilter defaults to Ordered.LOWEST_PRECEDENCE, while the
     * security chain sits at -100 (SecurityProperties.DEFAULT_FILTER_ORDER).
     * JwtAuthFilter is installed inside that chain and short-circuits an invalid
     * or expired token with a bare 401 — so with the default ordering this filter
     * never ran on exactly those responses, and every 401 reached the browser
     * with no Access-Control-Allow-Origin header at all.
     *
     * To JavaScript that is not a 401, it is an opaque network error
     * (net::ERR_FAILED), which means the frontend's refresh-on-401 interceptor
     * could never fire and a merely-expired access token logged the user out.
     * curl does not enforce CORS, which is why direct testing never caught it.
     *
     * CorsWebFilter writes its headers onto the response before delegating down
     * the chain, so running first means they survive any later short-circuit.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                // Any localhost port, not just 5173. Vite silently falls back to
                // 5174 (then 5175, ...) when its default port is taken, and with a
                // single pinned origin every API call from that fallback port fails
                // CORS — which surfaces in the browser as an opaque network error
                // indistinguishable from "the gateway is down". Matching the whole
                // dev port range removes a failure mode that costs real debugging
                // time and proves nothing.
                "http://localhost:[*]",
                "http://127.0.0.1:[*]",
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
