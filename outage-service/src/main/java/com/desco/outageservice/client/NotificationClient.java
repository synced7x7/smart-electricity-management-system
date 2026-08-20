package com.desco.outageservice.client;

import com.desco.outageservice.dto.request.BroadcastNotificationRequest;
import com.desco.outageservice.enums.Area;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestClient restClient;

    @Value("${notification.service.url:http://localhost:8084}")
    private String notificationServiceUrl;

    @Value("${internal.api-key:desco-internal-secret-key-12345}")
    private String internalApiKey;

    public void broadcastOutageNotification(Area area, String title, String message, UUID outageId) {
        String targetUrl = notificationServiceUrl + "/api/notifications/internal/broadcast";
        log.info("Dispatching outage notification to {} for area={}, outageId={}", targetUrl, area, outageId);

        BroadcastNotificationRequest request = BroadcastNotificationRequest.builder()
                .area(area)
                .type("OUTAGE")
                .title(title)
                .message(message)
                .relatedEntityId(outageId)
                .build();

        try {
            restClient.post()
                    .uri(targetUrl)
                    .header("X-Internal-Key", internalApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully broadcasted outage notification for area {}", area);
        } catch (Exception ex) {
            log.warn("Failed to dispatch notification to {}: {} (continuing best-effort)",
                    targetUrl, ex.getMessage());
        }
    }
}
