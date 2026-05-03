package com.subtrack.app.service;

import com.subtrack.app.dto.response.ReminderResponse;
import com.subtrack.app.entity.*;
import com.subtrack.app.exception.ResourceNotFoundException;
import com.subtrack.app.email.EmailService;
import com.subtrack.app.mapper.ReminderMapper;
import com.subtrack.app.repository.ReminderRepository;
import com.subtrack.app.repository.SubscriptionRepository;
import com.subtrack.app.util.BillingDateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ReminderMapper reminderMapper;
    private final EmailService emailService;

    /**
     * Generate reminders for all active subscriptions due in 1 or 3 days.
     * Idempotent — skips if reminder already exists for that (sub, type, date).
     */
    @Transactional
    public int generateReminders() {
        LocalDate today = LocalDate.now();
        LocalDate in3Days = today.plusDays(3);
        LocalDate in1Day  = today.plusDays(1);

        List<Subscription> upcoming = subscriptionRepository.findUpcomingBillings(today, in3Days);
        int created = 0;

        for (Subscription sub : upcoming) {
            long daysUntil = BillingDateCalculator.daysUntilBilling(sub.getNextBillingDate());

            if (daysUntil == 3) {
                created += createReminderIfAbsent(sub, ReminderType.THREE_DAYS, today);
            }
            if (daysUntil == 1) {
                created += createReminderIfAbsent(sub, ReminderType.ONE_DAY, today);
            }
        }

        log.info("Reminder generation complete — {} new reminders created", created);
        return created;
    }

    /**
     * Send all unsent reminders that are due today or overdue.
     * Idempotent — only sends if sentAt IS NULL.
     */
    @Transactional
    public int sendPendingReminders() {
        List<Reminder> pending = reminderRepository.findUnsentDueReminders(LocalDate.now());
        int sent = 0;

        for (Reminder reminder : pending) {
            try {
                emailService.sendReminderEmail(reminder);
                reminder.setSentAt(java.time.LocalDateTime.now());
                reminder.setEmailSentTo(reminder.getUser().getEmail());
                reminderRepository.save(reminder);
                sent++;
                log.info("Reminder sent: {} → {} ({})",
                        reminder.getSubscription().getName(),
                        reminder.getUser().getEmail(),
                        reminder.getReminderType());
            } catch (Exception e) {
                log.error("Failed to send reminder id={}: {}", reminder.getId(), e.getMessage());
                // Don't rethrow — continue processing other reminders
            }
        }

        log.info("Sent {} reminders", sent);
        return sent;
    }

    // ── Get reminders for user ───────────────────────────────
    @Transactional(readOnly = true)
    public List<ReminderResponse> getByUser(UUID userId) {
        return reminderRepository.findByUserIdOrderByScheduledForDesc(userId)
                .stream().map(reminderMapper::toResponse).collect(Collectors.toList());
    }

    // ── Manual test trigger ──────────────────────────────────
    @Transactional
    public ReminderResponse triggerTest(UUID userId, UUID subscriptionId) {
        Subscription sub = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", subscriptionId));

        Reminder reminder = Reminder.builder()
                .subscription(sub)
                .user(sub.getUser())
                .reminderType(ReminderType.THREE_DAYS)
                .scheduledFor(LocalDate.now())
                .build();

        reminderRepository.save(reminder);

        try {
            emailService.sendReminderEmail(reminder);
            reminder.setSentAt(java.time.LocalDateTime.now());
            reminder.setEmailSentTo(sub.getUser().getEmail());
            reminderRepository.save(reminder);
        } catch (Exception e) {
            log.error("Test reminder email failed: {}", e.getMessage());
        }

        return reminderMapper.toResponse(reminder);
    }

    // ── Private helpers ──────────────────────────────────────
    private int createReminderIfAbsent(Subscription sub, ReminderType type, LocalDate scheduledFor) {
        boolean exists = reminderRepository.existsBySubscriptionIdAndReminderTypeAndScheduledFor(
                sub.getId(), type, scheduledFor);

        if (exists) {
            log.debug("Reminder already exists: {} {} {}", sub.getName(), type, scheduledFor);
            return 0;
        }

        Reminder reminder = Reminder.builder()
                .subscription(sub)
                .user(sub.getUser())
                .reminderType(type)
                .scheduledFor(scheduledFor)
                .build();

        reminderRepository.save(reminder);
        log.debug("Created reminder: {} {} {}", sub.getName(), type, scheduledFor);
        return 1;
    }
}
