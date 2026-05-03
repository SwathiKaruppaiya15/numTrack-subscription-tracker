package com.subtrack.app.scheduler;

import com.subtrack.app.service.ReminderService;
import com.subtrack.app.service.UsageLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SubTrack Scheduler — Prompt 17 + Prompt 52 (safety)
 *
 * Jobs:
 *  1. Daily reminder generation  — 07:00 AM
 *  2. Daily reminder dispatch    — 08:00 AM
 *  3. Unused subscription check  — 02:00 AM
 *
 * Safety features:
 *  - AtomicBoolean lock prevents concurrent execution of same job
 *  - Idempotent: reminder generation checks DB before inserting
 *  - Full logging for every run
 *  - Failures are caught and logged — never crash the scheduler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubTrackScheduler {

    private final ReminderService reminderService;
    private final UsageLogService usageLogService;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    // Locks to prevent concurrent execution
    private final AtomicBoolean reminderGenerationRunning = new AtomicBoolean(false);
    private final AtomicBoolean reminderDispatchRunning   = new AtomicBoolean(false);
    private final AtomicBoolean unusedCheckRunning        = new AtomicBoolean(false);

    // ── Job 1: Generate reminders daily at 07:00 ─────────────
    @Scheduled(cron = "0 0 7 * * *", zone = "${app.scheduler.timezone:Asia/Kolkata}")
    public void generateReminders() {
        if (!schedulerEnabled) return;

        if (!reminderGenerationRunning.compareAndSet(false, true)) {
            log.warn("[SCHEDULER] generateReminders already running — skipping");
            return;
        }

        LocalDateTime start = LocalDateTime.now();
        log.info("[SCHEDULER] ▶ generateReminders started at {}", start);

        try {
            int count = reminderService.generateReminders();
            log.info("[SCHEDULER] ✓ generateReminders complete — {} reminders created in {}ms",
                    count, java.time.Duration.between(start, LocalDateTime.now()).toMillis());
        } catch (Exception e) {
            log.error("[SCHEDULER] ✗ generateReminders FAILED: {}", e.getMessage(), e);
        } finally {
            reminderGenerationRunning.set(false);
        }
    }

    // ── Job 2: Send pending reminders daily at 08:00 ─────────
    @Scheduled(cron = "0 0 8 * * *", zone = "${app.scheduler.timezone:Asia/Kolkata}")
    public void sendReminders() {
        if (!schedulerEnabled) return;

        if (!reminderDispatchRunning.compareAndSet(false, true)) {
            log.warn("[SCHEDULER] sendReminders already running — skipping");
            return;
        }

        LocalDateTime start = LocalDateTime.now();
        log.info("[SCHEDULER] ▶ sendReminders started at {}", start);

        try {
            int sent = reminderService.sendPendingReminders();
            log.info("[SCHEDULER] ✓ sendReminders complete — {} emails sent in {}ms",
                    sent, java.time.Duration.between(start, LocalDateTime.now()).toMillis());
        } catch (Exception e) {
            log.error("[SCHEDULER] ✗ sendReminders FAILED: {}", e.getMessage(), e);
        } finally {
            reminderDispatchRunning.set(false);
        }
    }

    // ── Job 3: Detect unused subscriptions daily at 02:00 ────
    @Scheduled(cron = "0 0 2 * * *", zone = "${app.scheduler.timezone:Asia/Kolkata}")
    public void detectUnusedSubscriptions() {
        if (!schedulerEnabled) return;

        if (!unusedCheckRunning.compareAndSet(false, true)) {
            log.warn("[SCHEDULER] detectUnused already running — skipping");
            return;
        }

        LocalDateTime start = LocalDateTime.now();
        log.info("[SCHEDULER] ▶ detectUnused started at {}", start);

        try {
            int marked = usageLogService.markUnusedAsAtRisk();
            log.info("[SCHEDULER] ✓ detectUnused complete — {} subscriptions marked AT_RISK in {}ms",
                    marked, java.time.Duration.between(start, LocalDateTime.now()).toMillis());
        } catch (Exception e) {
            log.error("[SCHEDULER] ✗ detectUnused FAILED: {}", e.getMessage(), e);
        } finally {
            unusedCheckRunning.set(false);
        }
    }
}
