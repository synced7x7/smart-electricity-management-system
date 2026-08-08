package com.desco.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

//api gateway security configuration
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) //no cookies so no csrf preferred
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) // using JWT
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable) //disable spring basic HTML login page
            .authorizeExchange(auth -> auth
                .pathMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/actuator/**").permitAll() //health/monitoring EP
                .anyExchange().authenticated() //don't require authorizing
            )
            .build();
    }
}
