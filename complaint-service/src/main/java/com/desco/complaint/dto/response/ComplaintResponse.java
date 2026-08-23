package com.desco.complaint.dto.response;

import com.desco.complaint.enums.Area;
import com.desco.complaint.enums.ComplaintCategory;
import com.desco.complaint.enums.ComplaintStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Area area;
    private ComplaintCategory category;
    private String title;
    private String description;
    private ComplaintStatus status;
    private String resolutionNotes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
