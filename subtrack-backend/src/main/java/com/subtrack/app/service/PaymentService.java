package com.subtrack.app.service;

import com.subtrack.app.dto.request.PaymentRequest;
import com.subtrack.app.dto.response.PaymentResponse;
import com.subtrack.app.email.EmailService;
import com.subtrack.app.entity.*;
import com.subtrack.app.exception.ResourceNotFoundException;
import com.subtrack.app.mapper.PaymentMapper;
import com.subtrack.app.repository.PaymentRepository;
import com.subtrack.app.repository.SubscriptionRepository;
import com.subtrack.app.repository.UserRepository;
import com.subtrack.app.util.BillingDateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final EmailService emailService;

    @Transactional
    public PaymentResponse recordPayment(UUID userId, PaymentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Subscription sub = subscriptionRepository.findByIdAndUserId(request.getSubscriptionId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.getSubscriptionId()));

        Payment payment = Payment.builder()
                .subscription(sub)
                .user(user)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : sub.getCurrency())
                .status(request.getStatus())
                .failureReason(request.getFailureReason())
                .build();

        paymentRepository.save(payment);

        // ── Post-payment logic ───────────────────────────────
        if (request.getStatus() == PaymentStatus.SUCCESS) {
            // Advance next billing date from actual payment date
            sub.setNextBillingDate(
                    BillingDateCalculator.calculateNextBillingDate(
                            sub.getNextBillingDate(), sub.getBillingCycle()));
            // Restore ACTIVE if it was AT_RISK
            if (sub.getStatus() == SubscriptionStatus.AT_RISK) {
                sub.setStatus(SubscriptionStatus.ACTIVE);
            }
            log.info("Payment SUCCESS for '{}' — next billing: {}", sub.getName(), sub.getNextBillingDate());

        } else if (request.getStatus() == PaymentStatus.FAILED) {
            // Mark subscription AT_RISK on failure
            sub.setStatus(SubscriptionStatus.AT_RISK);
            log.warn("Payment FAILED for '{}' — marked AT_RISK. Reason: {}", sub.getName(), request.getFailureReason());
            // Send failure alert email
            try { emailService.sendPaymentFailureAlert(sub, request.getFailureReason()); }
            catch (Exception e) { log.warn("Failure alert email failed: {}", e.getMessage()); }
        }

        subscriptionRepository.save(sub);
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByUser(UUID userId) {
        return paymentRepository.findByUserIdOrderByPaymentDateDesc(userId)
                .stream().map(paymentMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getBySubscription(UUID userId, UUID subscriptionId) {
        // Verify ownership
        subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));
        return paymentRepository.findBySubscriptionIdOrderByPaymentDateDesc(subscriptionId)
                .stream().map(paymentMapper::toResponse).collect(Collectors.toList());
    }
}
