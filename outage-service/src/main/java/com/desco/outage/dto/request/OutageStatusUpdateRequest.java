package com.desco.outage.dto.request;

import com.desco.outage.enums.OutageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutageStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OutageStatus status;
}
