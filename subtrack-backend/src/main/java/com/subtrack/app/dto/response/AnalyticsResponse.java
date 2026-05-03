package com.subtrack.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private BigDecimal totalMonthlySpend;
    private BigDecimal totalYearlyProjection;
    private int activeSubscriptions;
    private int atRiskSubscriptions;
    private BigDecimal potentialSavings;
    private List<CategoryBreakdown> categoryBreakdown;
    private List<MonthlyTrend> monthlyTrend;
    private List<UpcomingBill> upcomingBills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private BigDecimal amount;
        private double percentage;
        private int subscriptionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingBill {
        private UUID subscriptionId;
        private String name;
        private String category;
        private BigDecimal amount;
        private LocalDate nextBillingDate;
        private long daysUntilBilling;
    }
}
