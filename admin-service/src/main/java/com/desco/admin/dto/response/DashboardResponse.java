package com.desco.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Aggregate snapshot rendered by the admin dashboard. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private UserStats users;
    private PaymentStats payments;
    private ComplaintStats complaints;
    private OutageStats outages;
    private List<ServiceStatus> services;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private long total;
        private long active;
        private long inactive;
        private long admins;
        /** area label -> user count ("UNASSIGNED" for users with no area). */
        private Map<String, Long> byArea;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentStats {
        private long total;
        private long successful;
        private long pending;
        private long failed;
        private BigDecimal totalRevenue;
        private BigDecimal currentMonthRevenue;
        private String currentBillMonth;
        private Map<String, Long> byStatus;
        private List<MonthlyRevenue> recentMonths;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String billMonth;
        private BigDecimal revenue;
        private long payments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintStats {
        private long total;
        private long pending;
        private long inProgress;
        private long resolved;
        private long rejected;
        private Map<String, Long> byStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutageStats {
        private long total;
        private long scheduled;
        private long ongoing;
        private long resolved;
        private long cancelled;
        private Map<String, Long> byStatus;
        private List<OutageResponse> upcoming;
    }
}
