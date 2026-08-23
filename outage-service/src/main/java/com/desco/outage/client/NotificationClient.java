package com.desco.outage.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestClient notificationRestClient;

    public void sendOutageNotification(String area, String title, String message, UUID outageId) {
        try {
            Map<String, Object> payload = Map.of(
                    "targetArea", area,
                    "title", title,
                    "message", message,
                    "type", "OUTAGE_ALERT",
                    "referenceId", outageId != null ? outageId.toString() : ""
            );

            notificationRestClient.post()
                    .uri("/api/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Sent outage notification to area {}: {}", area, title);
        } catch (Exception e) {
            log.warn("Failed to dispatch notification for outage {} to area {}: {}", outageId, area, e.getMessage());
        }
    }
}
