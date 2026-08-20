package com.desco.outageservice.dto.request;

import com.desco.outageservice.enums.Area;
import com.desco.outageservice.enums.OutageStatus;
import com.desco.outageservice.enums.OutageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutageRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Area is required")
    private Area area;

    @NotNull(message = "Outage type is required")
    private OutageType type;

    private OutageStatus status;

    @NotNull(message = "Start time is required")
    private Instant startTime;

    private Instant estimatedEndTime;
}
