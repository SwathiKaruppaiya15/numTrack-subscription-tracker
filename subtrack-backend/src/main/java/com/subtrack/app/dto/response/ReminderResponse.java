package com.subtrack.app.dto.response;

import com.subtrack.app.entity.ReminderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class ReminderResponse {
    private UUID id;
    private UUID subscriptionId;
    private String subscriptionName;
    private BigDecimal subscriptionAmount;
    private ReminderType reminderType;
    private LocalDate scheduledFor;
    private LocalDateTime sentAt;
    private boolean sent;
}
