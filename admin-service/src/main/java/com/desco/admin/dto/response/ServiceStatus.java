package com.desco.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Live reachability of a sibling microservice. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatus {
    private String name;
    private String url;
    /** UP, DOWN, or UNKNOWN. */
    private String status;
    private Long responseTimeMs;
    private String detail;
}
