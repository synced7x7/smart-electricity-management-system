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
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/actuator/**").permitAll() //health
                .pathMatchers(HttpMethod.GET, "/api/outages/**").permitAll()
                .anyExchange().authenticated()
            )
            .addFilterAt(new JwtAuthFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
