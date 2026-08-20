package com.desco.outageservice.dto.request;

import com.desco.outageservice.enums.OutageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOutageStatusRequest {

    @NotNull(message = "Status is required")
    private OutageStatus status;
}
