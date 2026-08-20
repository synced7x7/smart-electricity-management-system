package com.desco.notificationservice.dto.response;

import com.desco.notificationservice.entity.Notification;
import com.desco.notificationservice.enums.Area;
import com.desco.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private Area area;
    private NotificationType type;
    private String title;
    private String message;
    private boolean isRead;
    private UUID relatedEntityId;
    private Instant createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        if (notification == null) return null;
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .area(notification.getArea())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .relatedEntityId(notification.getRelatedEntityId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
