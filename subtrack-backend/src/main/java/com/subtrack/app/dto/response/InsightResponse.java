package com.subtrack.app.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data @Builder
public class InsightResponse {
    private List<UnusedSubscription> unusedSubscriptions;
    private BigDecimal totalWastedMonthly;
    private List<String> spendingAlerts;
    private List<String> savingTips;

    @Data @Builder
    public static class UnusedSubscription {
        private java.util.UUID subscriptionId;
        private String name;
        private BigDecimal amount;
        private String lastUsedAt;
        private long daysSinceLastUse;
        private String suggestion;
    }
}
