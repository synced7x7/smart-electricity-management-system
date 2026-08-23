package com.desco.admin.dto.request;

import com.desco.admin.enums.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComplaintRequest {

    @NotNull(message = "status is required (OPEN, IN_PROGRESS, RESOLVED or CLOSED)")
    private ComplaintStatus status;

    @Size(max = 2000, message = "adminRemark must be at most 2000 characters")
    private String adminRemark;
}
