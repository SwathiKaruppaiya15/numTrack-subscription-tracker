package com.subtrack.app.mapper;

import com.subtrack.app.dto.response.PaymentResponse;
import com.subtrack.app.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .subscriptionId(payment.getSubscription().getId())
                .subscriptionName(payment.getSubscription().getName())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
