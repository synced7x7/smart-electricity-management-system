package com.desco.admin.client;

import com.desco.admin.dto.response.ServiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceHealthClient {

    private final RestClient healthRestClient;

    public ServiceStatus check(String name, String baseUrl) {
        long start = System.currentTimeMillis();
        try {
            String body = healthRestClient.get()
                    .uri(baseUrl + "/actuator/health")
                    .retrieve()
                    .body(String.class);

            long elapsed = System.currentTimeMillis() - start;
            boolean up = body != null && body.contains("\"status\":\"UP\"");

            return ServiceStatus.builder()
                    .name(name)
                    .url(baseUrl)
                    .status(up ? "UP" : "DOWN")
                    .responseTimeMs(elapsed)
                    .detail(up ? null : "Health endpoint reachable but not reporting UP")
                    .build();

        } catch (Exception ex) {
            log.debug("Health check failed for {} at {}: {}", name, baseUrl, ex.getMessage());
            return ServiceStatus.builder()
                    .name(name)
                    .url(baseUrl)
                    .status("DOWN")
                    .responseTimeMs(System.currentTimeMillis() - start)
                    .detail("Unreachable — service may not be implemented or started yet")
                    .build();
        }
    }
}
