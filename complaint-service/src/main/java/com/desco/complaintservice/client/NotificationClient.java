package com.desco.complaintservice.client;

import com.desco.complaintservice.dto.request.BroadcastNotificationRequest;
import com.desco.complaintservice.enums.Area;
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

    public void notifyComplaintStatusUpdate(UUID userId, Area area, String title, String message, UUID complaintId) {
        String targetUrl = notificationServiceUrl + "/api/notifications/internal/broadcast";
        log.info("Dispatching complaint notification to {} for userId={}, complaintId={}",
                targetUrl, userId, complaintId);

        BroadcastNotificationRequest request = BroadcastNotificationRequest.builder()
                .userId(userId)
                .area(area)
                .type("COMPLAINT_UPDATE")
                .title(title)
                .message(message)
                .relatedEntityId(complaintId)
                .build();

        try {
            restClient.post()
                    .uri(targetUrl)
                    .header("X-Internal-Key", internalApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully dispatched complaint notification for userId {}", userId);
        } catch (Exception ex) {
            log.warn("Failed to dispatch complaint notification to {}: {} (continuing best-effort)",
                    targetUrl, ex.getMessage());
        }
    }
}
