package com.subtrack.app.dto.response;

import com.subtrack.app.entity.BillingCycle;
import com.subtrack.app.entity.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class SubscriptionResponse {
    private UUID id;
    private String name;
    private String category;
    private BigDecimal amount;
    private String currency;
    private BillingCycle billingCycle;
    private LocalDate startDate;
    private LocalDate nextBillingDate;
    private SubscriptionStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private long usageCount;
    private long daysUntilBilling;
}
