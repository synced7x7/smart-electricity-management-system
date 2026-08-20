package com.desco.complaintservice.dto.request;

import com.desco.complaintservice.enums.Area;
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
    private String type; // "COMPLAINT_UPDATE"
    private String title;
    private String message;
    private UUID relatedEntityId;
}
