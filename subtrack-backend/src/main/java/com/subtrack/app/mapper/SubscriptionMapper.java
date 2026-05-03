package com.subtrack.app.mapper;

import com.subtrack.app.dto.response.SubscriptionResponse;
import com.subtrack.app.entity.Subscription;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class SubscriptionMapper {

    public SubscriptionResponse toResponse(Subscription sub,
                                           LocalDateTime lastUsedAt,
                                           long usageCount) {
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), sub.getNextBillingDate());

        return SubscriptionResponse.builder()
                .id(sub.getId())
                .name(sub.getName())
                .category(sub.getCategory())
                .amount(sub.getAmount())
                .currency(sub.getCurrency())
                .billingCycle(sub.getBillingCycle())
                .startDate(sub.getStartDate())
                .nextBillingDate(sub.getNextBillingDate())
                .status(sub.getStatus())
                .notes(sub.getNotes())
                .createdAt(sub.getCreatedAt())
                .lastUsedAt(lastUsedAt)
                .usageCount(usageCount)
                .daysUntilBilling(daysUntil)
                .build();
    }
}
