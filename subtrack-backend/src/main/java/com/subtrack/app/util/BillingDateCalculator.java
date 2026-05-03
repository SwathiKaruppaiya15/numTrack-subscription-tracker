package com.subtrack.app.util;

import com.subtrack.app.entity.BillingCycle;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Billing Date Calculator — handles all edge cases:
 * - Jan 31 + 1 month → Feb 28/29 (not crash)
 * - Leap year Feb 29 + 1 year → Feb 28 on non-leap year
 * - Monthly and yearly cycles
 * - Always returns a valid date
 */
@Slf4j
public class BillingDateCalculator {

    private BillingDateCalculator() {}

    /**
     * Calculate the next billing date from a given date.
     * Safely clamps to last day of month if needed.
     */
    public static LocalDate calculateNextBillingDate(LocalDate from, BillingCycle cycle) {
        if (from == null) throw new IllegalArgumentException("Base date cannot be null");
        if (cycle == null) throw new IllegalArgumentException("Billing cycle cannot be null");

        LocalDate next = switch (cycle) {
            case MONTHLY -> addMonthSafe(from, 1);
            case YEARLY  -> addYearSafe(from, 1);
        };

        log.debug("Next billing: {} + {} = {}", from, cycle, next);
        return next;
    }

    /**
     * Calculate the first billing date from a subscription start date.
     * If start date is today or in the past, advance to next cycle.
     */
    public static LocalDate calculateFirstBillingDate(LocalDate startDate, BillingCycle cycle) {
        if (startDate == null) throw new IllegalArgumentException("Start date cannot be null");

        LocalDate today = LocalDate.now();

        // If start date is in the future, first billing is start date
        if (startDate.isAfter(today)) {
            return startDate;
        }

        // Advance from start date until we get a future date
        LocalDate billing = startDate;
        int maxIterations = 1500; // safety cap (~4 years of monthly)
        int iterations = 0;

        while (!billing.isAfter(today) && iterations < maxIterations) {
            billing = calculateNextBillingDate(billing, cycle);
            iterations++;
        }

        return billing;
    }

    /**
     * Add months safely — clamps to last day of resulting month.
     * Example: Jan 31 + 1 month = Feb 28 (or 29 on leap year)
     */
    public static LocalDate addMonthSafe(LocalDate date, int months) {
        int targetMonth = date.getMonthValue() + months;
        int targetYear  = date.getYear() + (targetMonth - 1) / 12;
        targetMonth     = ((targetMonth - 1) % 12) + 1;

        int maxDay = YearMonth.of(targetYear, targetMonth).lengthOfMonth();
        int day    = Math.min(date.getDayOfMonth(), maxDay);

        return LocalDate.of(targetYear, targetMonth, day);
    }

    /**
     * Add years safely — handles Feb 29 on non-leap years.
     * Example: Feb 29, 2024 + 1 year = Feb 28, 2025
     */
    public static LocalDate addYearSafe(LocalDate date, int years) {
        int targetYear = date.getYear() + years;
        int maxDay     = YearMonth.of(targetYear, date.getMonthValue()).lengthOfMonth();
        int day        = Math.min(date.getDayOfMonth(), maxDay);
        return LocalDate.of(targetYear, date.getMonthValue(), day);
    }

    /**
     * How many days until the next billing date from today.
     */
    public static long daysUntilBilling(LocalDate nextBillingDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextBillingDate);
    }

    /**
     * Check if a billing date is within N days from today.
     */
    public static boolean isDueWithinDays(LocalDate nextBillingDate, int days) {
        LocalDate today    = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        return !nextBillingDate.isBefore(today) && !nextBillingDate.isAfter(deadline);
    }
}
