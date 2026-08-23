package com.desco.admin.dto.response;

import com.desco.admin.enums.AreaName;
import com.desco.admin.enums.OutageStatus;
import com.desco.admin.enums.OutageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutageResponse {
    private UUID id;
    private String title;
    private String description;
    private AreaName area;
    private OutageType type;
    private OutageStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
