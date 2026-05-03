package com.subtrack.app.service;

import com.subtrack.app.dto.request.UsageLogRequest;
import com.subtrack.app.dto.response.UsageLogResponse;
import com.subtrack.app.entity.Subscription;
import com.subtrack.app.entity.SubscriptionStatus;
import com.subtrack.app.entity.UsageLog;
import com.subtrack.app.entity.User;
import com.subtrack.app.exception.ResourceNotFoundException;
import com.subtrack.app.repository.SubscriptionRepository;
import com.subtrack.app.repository.UsageLogRepository;
import com.subtrack.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    private static final int UNUSED_THRESHOLD_DAYS = 30;

    // ── Log usage ────────────────────────────────────────────
    @Transactional
    public UsageLogResponse logUsage(UUID userId, UsageLogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Subscription sub = subscriptionRepository.findByIdAndUserId(request.getSubscriptionId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", request.getSubscriptionId()));

        UsageLog usageLog = UsageLog.builder()
                .subscription(sub)
                .user(user)
                .usedAt(LocalDateTime.now())
                .notes(request.getNotes())
                .build();

        usageLogRepository.save(usageLog);

        // If subscription was AT_RISK due to non-use, restore to ACTIVE
        if (sub.getStatus() == SubscriptionStatus.AT_RISK) {
            sub.setStatus(SubscriptionStatus.ACTIVE);
            subscriptionRepository.save(sub);
        }

        log.info("Usage logged for subscription '{}' by user {}", sub.getName(), userId);
        return toResponse(usageLog);
    }

    // ── Get usage logs for a subscription ───────────────────
    @Transactional(readOnly = true)
    public List<UsageLogResponse> getBySubscription(UUID userId, UUID subscriptionId) {
        subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));
        return usageLogRepository.findBySubscriptionIdOrderByUsedAtDesc(subscriptionId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Detect unused subscriptions ──────────────────────────
    @Transactional(readOnly = true)
    public List<Subscription> detectUnused(UUID userId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(UNUSED_THRESHOLD_DAYS);
        return subscriptionRepository.findUnusedByUser(userId, cutoff);
    }

    // ── Mark unused subscriptions AT_RISK (called by scheduler) ─
    @Transactional
    public int markUnusedAsAtRisk() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(UNUSED_THRESHOLD_DAYS);
        List<Subscription> unused = subscriptionRepository.findAllUnused(cutoff);
        int count = 0;
        for (Subscription sub : unused) {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                sub.setStatus(SubscriptionStatus.AT_RISK);
                subscriptionRepository.save(sub);
                count++;
                log.info("Marked AT_RISK (unused): {}", sub.getName());
            }
        }
        return count;
    }

    // ── Helper ───────────────────────────────────────────────
    private UsageLogResponse toResponse(UsageLog ul) {
        return UsageLogResponse.builder()
                .id(ul.getId())
                .subscriptionId(ul.getSubscription().getId())
                .subscriptionName(ul.getSubscription().getName())
                .usedAt(ul.getUsedAt())
                .notes(ul.getNotes())
                .build();
    }
}
