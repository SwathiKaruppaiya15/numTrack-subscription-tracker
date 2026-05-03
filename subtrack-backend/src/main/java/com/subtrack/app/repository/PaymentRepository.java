package com.subtrack.app.repository;

import com.subtrack.app.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByUserIdOrderByPaymentDateDesc(UUID userId);

    List<Payment> findBySubscriptionIdOrderByPaymentDateDesc(UUID subscriptionId);

    // Monthly spend for a user
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.user.id = :userId " +
           "AND p.status = 'SUCCESS' " +
           "AND p.paymentDate BETWEEN :from AND :to")
    BigDecimal sumSuccessfulPayments(
            @Param("userId") UUID userId,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to);

    // Category breakdown
    @Query("SELECT s.category, COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "JOIN p.subscription s " +
           "WHERE p.user.id = :userId " +
           "AND p.status = 'SUCCESS' " +
           "AND p.paymentDate BETWEEN :from AND :to " +
           "GROUP BY s.category")
    List<Object[]> categoryBreakdown(
            @Param("userId") UUID userId,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to);

    // Monthly trend (last N months)
    @Query(value = "SELECT TO_CHAR(payment_date, 'YYYY-MM') AS month, SUM(amount) " +
                   "FROM payments " +
                   "WHERE user_id = :userId AND status = 'SUCCESS' " +
                   "AND payment_date >= :from " +
                   "GROUP BY month ORDER BY month",
           nativeQuery = true)
    List<Object[]> monthlyTrend(
            @Param("userId") UUID userId,
            @Param("from")   LocalDateTime from);
}
