package com.desco.config;

import com.desco.filter.JwtAuthFilter;
import com.desco.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;

//api gateway security configuration
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) //no cookies so no csrf preferred
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // using JWT
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable) //disable spring basic HTML login page
            .authorizeExchange(auth -> auth
                // CORS preflight has no Authorization header by design; rejecting it
                // here would make every cross-origin request to a protected route fail
                // before CorsWebFilter ever gets to answer it.
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/actuator/**").permitAll() //health/monitoring EP
                .pathMatchers(HttpMethod.GET, "/api/outages/**").permitAll()
                .anyExchange().authenticated() //don't require authorizing
            )
            // Runs inside Security's own pipeline, before the authorizeExchange
            // check above evaluates — a standalone @Component WebFilter would run
            // after it and never get the chance to authenticate the request.
            .addFilterAt(new JwtAuthFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
