package com.desco.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory sliding-window rate limiter (per IP).
 *
 * Limits: 200 requests / 60-second window per IP address.
 *
 * NOTE: For production at scale, replace with Redis-backed
 *       RequestRateLimiter (spring-cloud-gateway-redis).
 *       This in-memory version is suitable for single-instance
 *       deployments on Render free tier.
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final int    MAX_REQUESTS  = 200;
    private static final long   WINDOW_MS     = 60_000L;  // 1 minute
    private static final String UNKNOWN_IP    = "unknown";

    // ip → [requestCount, windowStartMs]
    private final ConcurrentHashMap<String, long[]> ipWindowMap = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        // Run before JWT filter (order -2)
        return -3;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = resolveClientIp(exchange);
        long   now = System.currentTimeMillis();

        long[] window = ipWindowMap.compute(ip, (key, existing) -> {
            if (existing == null || (now - existing[1]) > WINDOW_MS) {
                // New window
                return new long[]{1, now};
            }
            existing[0]++;
            return existing;
        });

        long requestCount = window[0];

        if (requestCount > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for IP: {} ({} requests in window)", ip, requestCount);
            return tooManyRequests(exchange, ip);
        }

        // Add rate-limit response headers
        exchange.getResponse().getHeaders().add("X-RateLimit-Limit",     String.valueOf(MAX_REQUESTS));
        exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(MAX_REQUESTS - requestCount));

        return chain.filter(exchange);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String resolveClientIp(ServerWebExchange exchange) {
        // Check X-Forwarded-For first (Render/Vercel proxies set this)
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddr = exchange.getRequest().getRemoteAddress();
        return remoteAddr != null ? remoteAddr.getAddress().getHostAddress() : UNKNOWN_IP;
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, String ip) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().add("Retry-After", "60");

        String body = String.format(
            "{\"status\":429,\"error\":\"Too Many Requests\"," +
            "\"message\":\"Rate limit exceeded. Max %d requests per minute.\"," +
            "\"timestamp\":\"%s\"}",
            MAX_REQUESTS, Instant.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
