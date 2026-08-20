package com.desco.notificationservice.dto.request;

import com.desco.notificationservice.enums.Area;
import com.desco.notificationservice.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastNotificationRequest {

    private UUID userId;
    private Area area;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotBlank(message = "Notification title is required")
    private String title;

    @NotBlank(message = "Notification message is required")
    private String message;

    private UUID relatedEntityId;
}
