package com.desco.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatus {
    private String name;
    private String url;
    private String status;
    private Long responseTimeMs;
    private String detail;
}
