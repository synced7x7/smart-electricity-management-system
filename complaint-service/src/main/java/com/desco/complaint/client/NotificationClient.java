package com.desco.complaint.client;

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

    public void sendComplaintNotification(UUID targetUserId, String targetArea, String title, String message, UUID complaintId) {
        try {
            Map<String, Object> payload = Map.of(
                    "targetUserId", targetUserId != null ? targetUserId.toString() : "",
                    "targetArea", targetArea != null ? targetArea : "",
                    "title", title,
                    "message", message,
                    "type", "COMPLAINT_UPDATE",
                    "referenceId", complaintId != null ? complaintId.toString() : ""
            );

            notificationRestClient.post()
                    .uri("/api/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Sent complaint notification for user {}: {}", targetUserId, title);
        } catch (Exception e) {
            log.warn("Failed to dispatch notification for complaint {} to user {}: {}", complaintId, targetUserId, e.getMessage());
        }
    }
}
