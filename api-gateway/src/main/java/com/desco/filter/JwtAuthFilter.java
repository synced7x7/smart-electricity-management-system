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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

// Wired into the security chain by SecurityConfig via
// addFilterAt(..., SecurityWebFiltersOrder.AUTHENTICATION) — deliberately NOT a
// @Component. Spring Boot auto-registers every WebFilter bean as an independent
// global filter ordered by its own Ordered value; Spring Security's own chain is
// fixed at @Order(-100) (SecurityProperties.DEFAULT_FILTER_ORDER), which runs
// before any global filter with a "less negative" order. A standalone bean here
// would run after Security's authorizeExchange() had already rejected the
// exchange for lacking an authenticated context — the filter needs to run
// inside Security's own pipeline, before that check, not beside it.
@Slf4j
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
