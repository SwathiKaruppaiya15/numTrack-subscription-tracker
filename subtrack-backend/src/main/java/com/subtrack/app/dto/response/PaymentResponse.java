package com.subtrack.app.dto.response;

import com.subtrack.app.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class PaymentResponse {
    private UUID id;
    private UUID subscriptionId;
    private String subscriptionName;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime paymentDate;
}
