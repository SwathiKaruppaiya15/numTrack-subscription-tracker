package com.subtrack.app;

import com.subtrack.app.entity.BillingCycle;
import com.subtrack.app.util.BillingDateCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prompt 49 + Prompt 53 — Billing edge case tests
 */
class BillingDateCalculatorTest {

    // ── Monthly edge cases ───────────────────────────────────

    @Test
    @DisplayName("Jan 31 + 1 month = Feb 28 (non-leap year)")
    void jan31PlusOneMonth_nonLeap() {
        LocalDate jan31 = LocalDate.of(2025, 1, 31);
        LocalDate result = BillingDateCalculator.addMonthSafe(jan31, 1);
        assertThat(result).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Jan 31 + 1 month = Feb 29 (leap year 2024)")
    void jan31PlusOneMonth_leapYear() {
        LocalDate jan31 = LocalDate.of(2024, 1, 31);
        LocalDate result = BillingDateCalculator.addMonthSafe(jan31, 1);
        assertThat(result).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    @DisplayName("Mar 31 + 1 month = Apr 30")
    void mar31PlusOneMonth() {
        LocalDate mar31 = LocalDate.of(2025, 3, 31);
        LocalDate result = BillingDateCalculator.addMonthSafe(mar31, 1);
        assertThat(result).isEqualTo(LocalDate.of(2025, 4, 30));
    }

    @Test
    @DisplayName("Dec 31 + 1 month = Jan 31 next year")
    void dec31PlusOneMonth() {
        LocalDate dec31 = LocalDate.of(2025, 12, 31);
        LocalDate result = BillingDateCalculator.addMonthSafe(dec31, 1);
        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 31));
    }

    @Test
    @DisplayName("Normal date: May 15 + 1 month = Jun 15")
    void normalMonthlyBilling() {
        LocalDate may15 = LocalDate.of(2025, 5, 15);
        LocalDate result = BillingDateCalculator.addMonthSafe(may15, 1);
        assertThat(result).isEqualTo(LocalDate.of(2025, 6, 15));
    }

    // ── Yearly edge cases ────────────────────────────────────

    @Test
    @DisplayName("Feb 29 (leap) + 1 year = Feb 28 (non-leap)")
    void feb29LeapPlusOneYear() {
        LocalDate feb29 = LocalDate.of(2024, 2, 29);
        LocalDate result = BillingDateCalculator.addYearSafe(feb29, 1);
        assertThat(result).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Feb 29 (leap) + 4 years = Feb 29 (next leap)")
    void feb29LeapPlusFourYears() {
        LocalDate feb29 = LocalDate.of(2024, 2, 29);
        LocalDate result = BillingDateCalculator.addYearSafe(feb29, 4);
        assertThat(result).isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    @DisplayName("Normal yearly: May 15 + 1 year = May 15 next year")
    void normalYearlyBilling() {
        LocalDate may15 = LocalDate.of(2025, 5, 15);
        LocalDate result = BillingDateCalculator.addYearSafe(may15, 1);
        assertThat(result).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    // ── calculateNextBillingDate ─────────────────────────────

    @Test
    @DisplayName("MONTHLY cycle uses addMonthSafe")
    void nextBillingMonthly() {
        LocalDate date = LocalDate.of(2025, 1, 31);
        LocalDate next = BillingDateCalculator.calculateNextBillingDate(date, BillingCycle.MONTHLY);
        assertThat(next).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("YEARLY cycle uses addYearSafe")
    void nextBillingYearly() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        LocalDate next = BillingDateCalculator.calculateNextBillingDate(date, BillingCycle.YEARLY);
        assertThat(next).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    @DisplayName("Null date throws IllegalArgumentException")
    void nullDateThrows() {
        assertThatThrownBy(() -> BillingDateCalculator.calculateNextBillingDate(null, BillingCycle.MONTHLY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Null cycle throws IllegalArgumentException")
    void nullCycleThrows() {
        assertThatThrownBy(() -> BillingDateCalculator.calculateNextBillingDate(LocalDate.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── isDueWithinDays ──────────────────────────────────────

    @Test
    @DisplayName("isDueWithinDays: tomorrow is within 3 days")
    void isDueWithin3Days() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThat(BillingDateCalculator.isDueWithinDays(tomorrow, 3)).isTrue();
    }

    @Test
    @DisplayName("isDueWithinDays: 10 days away is NOT within 3 days")
    void isNotDueWithin3Days() {
        LocalDate tenDays = LocalDate.now().plusDays(10);
        assertThat(BillingDateCalculator.isDueWithinDays(tenDays, 3)).isFalse();
    }

    @Test
    @DisplayName("isDueWithinDays: yesterday is NOT within range (past)")
    void pastDateNotDue() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThat(BillingDateCalculator.isDueWithinDays(yesterday, 3)).isFalse();
    }

    // ── calculateFirstBillingDate ────────────────────────────

    @Test
    @DisplayName("Future start date returns start date as first billing")
    void futureStartDate() {
        LocalDate future = LocalDate.now().plusDays(10);
        LocalDate result = BillingDateCalculator.calculateFirstBillingDate(future, BillingCycle.MONTHLY);
        assertThat(result).isEqualTo(future);
    }

    @Test
    @DisplayName("Past start date advances to future billing date")
    void pastStartDateAdvances() {
        LocalDate past = LocalDate.now().minusMonths(2);
        LocalDate result = BillingDateCalculator.calculateFirstBillingDate(past, BillingCycle.MONTHLY);
        assertThat(result).isAfter(LocalDate.now());
    }
}
