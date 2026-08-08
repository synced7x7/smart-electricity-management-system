package com.desco.filter;

import com.desco.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    //Not returning actual data (async)
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange,
                                      @NonNull WebFilterChain chain) {

        String authHeader = exchange.getRequest()
            .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            log.warn("Invalid JWT received at gateway");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String email = jwtUtil.getEmail(token);
        String role  = jwtUtil.getRole(token);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            email, null,
            role != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                : List.of()
        );

        log.debug("Authenticated user '{}' with role '{}' at gateway", email, role);

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}
