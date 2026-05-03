package com.subtrack.app.service;

import com.subtrack.app.dto.request.SubscriptionRequest;
import com.subtrack.app.dto.response.SubscriptionResponse;
import com.subtrack.app.entity.*;
import com.subtrack.app.exception.BadRequestException;
import com.subtrack.app.exception.DuplicateResourceException;
import com.subtrack.app.exception.ResourceNotFoundException;
import com.subtrack.app.mapper.SubscriptionMapper;
import com.subtrack.app.repository.SubscriptionRepository;
import com.subtrack.app.repository.UsageLogRepository;
import com.subtrack.app.repository.UserRepository;
import com.subtrack.app.util.BillingDateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final UsageLogRepository usageLogRepository;
    private final SubscriptionMapper subscriptionMapper;

    // ── Create ───────────────────────────────────────────────
    @Transactional
    public SubscriptionResponse create(UUID userId, SubscriptionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Prevent duplicate subscription name per user
        if (subscriptionRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new DuplicateResourceException(
                    "Subscription '" + request.getName() + "' already exists for this user");
        }

        // Validate start date not too far in the past
        if (request.getStartDate().isBefore(LocalDate.now().minusYears(10))) {
            throw new BadRequestException("Start date cannot be more than 10 years in the past");
        }

        LocalDate nextBilling = BillingDateCalculator.calculateFirstBillingDate(
                request.getStartDate(), request.getBillingCycle());

        Subscription subscription = Subscription.builder()
                .user(user)
                .name(request.getName())
                .category(request.getCategory())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .billingCycle(request.getBillingCycle())
                .startDate(request.getStartDate())
                .nextBillingDate(nextBilling)
                .status(SubscriptionStatus.ACTIVE)
                .notes(request.getNotes())
                .build();

        subscriptionRepository.save(subscription);
        log.info("Subscription created: {} for user {}", subscription.getName(), userId);

        return toResponse(subscription);
    }

    // ── Get all for user ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAllByUser(UUID userId) {
        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Get by ID ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public SubscriptionResponse getById(UUID userId, UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));
        return toResponse(sub);
    }

    // ── Update ───────────────────────────────────────────────
    @Transactional
    public SubscriptionResponse update(UUID userId, UUID subscriptionId, SubscriptionRequest request) {
        Subscription sub = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));

        // Check duplicate name (excluding self)
        if (!sub.getName().equals(request.getName()) &&
                subscriptionRepository.existsByUserIdAndNameAndIdNot(userId, request.getName(), subscriptionId)) {
            throw new DuplicateResourceException("Subscription name already in use: " + request.getName());
        }

        // Recalculate billing date if cycle changed
        boolean cycleChanged = !sub.getBillingCycle().equals(request.getBillingCycle());
        if (cycleChanged) {
            LocalDate newNext = BillingDateCalculator.calculateFirstBillingDate(
                    request.getStartDate(), request.getBillingCycle());
            sub.setNextBillingDate(newNext);
        }

        sub.setName(request.getName());
        sub.setCategory(request.getCategory());
        sub.setAmount(request.getAmount());
        sub.setCurrency(request.getCurrency() != null ? request.getCurrency() : sub.getCurrency());
        sub.setBillingCycle(request.getBillingCycle());
        sub.setStartDate(request.getStartDate());
        sub.setNotes(request.getNotes());

        subscriptionRepository.save(sub);
        log.info("Subscription updated: {} for user {}", sub.getName(), userId);

        return toResponse(sub);
    }

    // ── Cancel ───────────────────────────────────────────────
    @Transactional
    public SubscriptionResponse cancel(UUID userId, UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));

        if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BadRequestException("Subscription is already cancelled");
        }

        sub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(sub);
        log.info("Subscription cancelled: {} for user {}", sub.getName(), userId);

        return toResponse(sub);
    }

    // ── Delete ───────────────────────────────────────────────
    @Transactional
    public void delete(UUID userId, UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));
        subscriptionRepository.delete(sub);
        log.info("Subscription deleted: {} for user {}", sub.getName(), userId);
    }

    // ── Helper ───────────────────────────────────────────────
    private SubscriptionResponse toResponse(Subscription sub) {
        var lastUsed = usageLogRepository.findLastUsedAt(sub.getId()).orElse(null);
        long usageCount = usageLogRepository.countBySubscriptionId(sub.getId());
        return subscriptionMapper.toResponse(sub, lastUsed, usageCount);
    }
}
