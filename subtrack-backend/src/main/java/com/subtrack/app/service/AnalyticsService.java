package com.subtrack.app.service;

import com.subtrack.app.dto.response.AnalyticsResponse;
import com.subtrack.app.dto.response.InsightResponse;
import com.subtrack.app.entity.BillingCycle;
import com.subtrack.app.entity.Subscription;
import com.subtrack.app.entity.SubscriptionStatus;
import com.subtrack.app.repository.PaymentRepository;
import com.subtrack.app.repository.SubscriptionRepository;
import com.subtrack.app.repository.UsageLogRepository;
import com.subtrack.app.util.BillingDateCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UsageLogRepository usageLogRepository;

    private static final int UNUSED_DAYS = 30;

    // ── Full analytics dashboard ─────────────────────────────
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(UUID userId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd   = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Monthly spend (actual payments)
        BigDecimal monthlySpend = paymentRepository.sumSuccessfulPayments(userId, monthStart, monthEnd);

        // Active subscriptions
        List<Subscription> active = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        List<Subscription> atRisk = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.AT_RISK);

        // Estimated monthly total from active subscriptions
        BigDecimal estimatedMonthly = active.stream()
                .map(s -> s.getBillingCycle() == BillingCycle.MONTHLY
                        ? s.getAmount()
                        : s.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Yearly projection
        BigDecimal yearlyProjection = estimatedMonthly.multiply(BigDecimal.valueOf(12));

        // Potential savings (AT_RISK subscriptions)
        BigDecimal potentialSavings = atRisk.stream()
                .map(s -> s.getBillingCycle() == BillingCycle.MONTHLY
                        ? s.getAmount()
                        : s.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Category breakdown
        List<Object[]> rawBreakdown = paymentRepository.categoryBreakdown(userId, monthStart, monthEnd);
        List<AnalyticsResponse.CategoryBreakdown> breakdown = buildCategoryBreakdown(rawBreakdown, monthlySpend);

        // Monthly trend (last 6 months)
        LocalDateTime trendFrom = LocalDateTime.now().minusMonths(6);
        List<Object[]> rawTrend = paymentRepository.monthlyTrend(userId, trendFrom);
        List<AnalyticsResponse.MonthlyTrend> trend = rawTrend.stream()
                .map(row -> AnalyticsResponse.MonthlyTrend.builder()
                        .month((String) row[0])
                        .amount(new BigDecimal(row[1].toString()))
                        .build())
                .collect(Collectors.toList());

        // Upcoming bills (next 30 days)
        List<Subscription> allSubs = subscriptionRepository.findByUserId(userId);
        List<AnalyticsResponse.UpcomingBill> upcoming = allSubs.stream()
                .filter(s -> s.getStatus() != SubscriptionStatus.CANCELLED)
                .filter(s -> BillingDateCalculator.isDueWithinDays(s.getNextBillingDate(), 30))
                .sorted(Comparator.comparing(Subscription::getNextBillingDate))
                .map(s -> AnalyticsResponse.UpcomingBill.builder()
                        .subscriptionId(s.getId())
                        .name(s.getName())
                        .category(s.getCategory())
                        .amount(s.getAmount())
                        .nextBillingDate(s.getNextBillingDate())
                        .daysUntilBilling(BillingDateCalculator.daysUntilBilling(s.getNextBillingDate()))
                        .build())
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .totalMonthlySpend(monthlySpend)
                .totalYearlyProjection(yearlyProjection)
                .activeSubscriptions(active.size())
                .atRiskSubscriptions(atRisk.size())
                .potentialSavings(potentialSavings)
                .categoryBreakdown(breakdown)
                .monthlyTrend(trend)
                .upcomingBills(upcoming)
                .build();
    }

    // ── Smart Insights (Prompt 19) ───────────────────────────
    @Transactional(readOnly = true)
    public InsightResponse getInsights(UUID userId) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(UNUSED_DAYS);
        List<Subscription> unused = subscriptionRepository.findUnusedByUser(userId, cutoff);

        List<InsightResponse.UnusedSubscription> unusedList = unused.stream().map(sub -> {
            Optional<LocalDateTime> lastUsed = usageLogRepository.findLastUsedAt(sub.getId());
            long daysSince = lastUsed
                    .map(lu -> ChronoUnit.DAYS.between(lu, LocalDateTime.now()))
                    .orElse(Long.MAX_VALUE);

            String lastUsedStr = lastUsed
                    .map(lu -> lu.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .orElse("Never used");

            return InsightResponse.UnusedSubscription.builder()
                    .subscriptionId(sub.getId())
                    .name(sub.getName())
                    .amount(sub.getAmount())
                    .lastUsedAt(lastUsedStr)
                    .daysSinceLastUse(daysSince == Long.MAX_VALUE ? -1 : daysSince)
                    .suggestion("Consider cancelling '" + sub.getName() +
                            "' — you haven't used it in " +
                            (daysSince == Long.MAX_VALUE ? "a long time" : daysSince + " days"))
                    .build();
        }).collect(Collectors.toList());

        BigDecimal totalWaste = unused.stream()
                .map(s -> s.getBillingCycle() == BillingCycle.MONTHLY
                        ? s.getAmount()
                        : s.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Spending alerts
        List<String> alerts = new ArrayList<>();
        if (!unused.isEmpty()) {
            alerts.add("You have " + unused.size() + " unused subscription(s) costing ₹" +
                    totalWaste.setScale(0, RoundingMode.HALF_UP) + "/month");
        }

        // Saving tips
        List<String> tips = new ArrayList<>();
        List<Subscription> allSubs = subscriptionRepository.findByUserId(userId);
        long monthlyCount = allSubs.stream()
                .filter(s -> s.getBillingCycle() == BillingCycle.MONTHLY && s.getStatus() == SubscriptionStatus.ACTIVE)
                .count();
        if (monthlyCount > 0) {
            tips.add("Switch monthly subscriptions to yearly to save up to 20%");
        }
        tips.add("Review your subscriptions every month to avoid bill creep");

        return InsightResponse.builder()
                .unusedSubscriptions(unusedList)
                .totalWastedMonthly(totalWaste)
                .spendingAlerts(alerts)
                .savingTips(tips)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────
    private List<AnalyticsResponse.CategoryBreakdown> buildCategoryBreakdown(
            List<Object[]> raw, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return Collections.emptyList();

        return raw.stream().map(row -> {
            BigDecimal amount = new BigDecimal(row[1].toString());
            double pct = amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            return AnalyticsResponse.CategoryBreakdown.builder()
                    .category((String) row[0])
                    .amount(amount)
                    .percentage(Math.round(pct * 10.0) / 10.0)
                    .subscriptionCount(0) // enriched separately if needed
                    .build();
        }).collect(Collectors.toList());
    }
}
