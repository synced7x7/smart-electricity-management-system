package com.desco.outageservice.dto.response;

import com.desco.outageservice.entity.Outage;
import com.desco.outageservice.enums.Area;
import com.desco.outageservice.enums.OutageStatus;
import com.desco.outageservice.enums.OutageType;
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
public class OutageResponse {

    private UUID id;
    private String title;
    private String description;
    private Area area;
    private OutageType type;
    private OutageStatus status;
    private Instant startTime;
    private Instant estimatedEndTime;
    private Instant actualEndTime;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public static OutageResponse fromEntity(Outage outage) {
        if (outage == null) return null;
        return OutageResponse.builder()
                .id(outage.getId())
                .title(outage.getTitle())
                .description(outage.getDescription())
                .area(outage.getArea())
                .type(outage.getType())
                .status(outage.getStatus())
                .startTime(outage.getStartTime())
                .estimatedEndTime(outage.getEstimatedEndTime())
                .actualEndTime(outage.getActualEndTime())
                .createdBy(outage.getCreatedBy())
                .createdAt(outage.getCreatedAt())
                .updatedAt(outage.getUpdatedAt())
                .build();
    }
}
