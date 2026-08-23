package com.desco.admin.dto.request;

import com.desco.admin.enums.OutageStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOutageStatusRequest {

    @NotNull(message = "status is required (SCHEDULED, ONGOING, COMPLETED or CANCELLED)")
    private OutageStatus status;
}
