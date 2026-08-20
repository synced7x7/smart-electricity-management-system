package com.desco.complaintservice.dto.response;

import com.desco.complaintservice.entity.Complaint;
import com.desco.complaintservice.enums.Area;
import com.desco.complaintservice.enums.ComplaintStatus;
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
public class ComplaintResponse {

    private UUID id;
    private UUID userId;
    private Area area;
    private String subject;
    private String description;
    private ComplaintStatus status;
    private String adminRemarks;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;

    public static ComplaintResponse fromEntity(Complaint complaint) {
        if (complaint == null) return null;
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .userId(complaint.getUserId())
                .area(complaint.getArea())
                .subject(complaint.getSubject())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .adminRemarks(complaint.getAdminRemarks())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .build();
    }
}
