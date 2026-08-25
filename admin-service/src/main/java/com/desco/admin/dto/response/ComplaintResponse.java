package com.desco.admin.dto.response;

import com.desco.admin.enums.AreaName;
import com.desco.admin.enums.ComplaintStatus;
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
public class ComplaintResponse {
    private UUID id;
    private UUID userId;
    private String subject;
    private String description;
    private AreaName area;
    private ComplaintStatus status;
    private String adminRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
