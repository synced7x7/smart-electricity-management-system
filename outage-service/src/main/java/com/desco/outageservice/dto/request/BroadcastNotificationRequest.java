package com.desco.outageservice.dto.request;

import com.desco.outageservice.enums.Area;
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
    private String type; // "OUTAGE"
    private String title;
    private String message;
    private UUID relatedEntityId;
}
