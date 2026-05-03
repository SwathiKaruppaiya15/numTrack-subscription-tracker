package com.subtrack.app.repository;

import com.subtrack.app.entity.Subscription;
import com.subtrack.app.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserId(UUID userId);

    List<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    Optional<Subscription> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndName(UUID userId, String name);

    boolean existsByUserIdAndNameAndIdNot(UUID userId, String name, UUID id);

    // Subscriptions due within N days (for scheduler)
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.nextBillingDate BETWEEN :from AND :to")
    List<Subscription> findUpcomingBillings(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to);

    // Unused subscriptions: no usage log in last 30 days
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.user.id = :userId " +
           "AND (SELECT MAX(u.usedAt) FROM UsageLog u WHERE u.subscription = s) < :cutoff " +
           "OR (SELECT COUNT(u) FROM UsageLog u WHERE u.subscription = s) = 0")
    List<Subscription> findUnusedByUser(
            @Param("userId") UUID userId,
            @Param("cutoff") java.time.LocalDateTime cutoff);

    // All active subscriptions with no usage in 30 days (for scheduler)
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND ((SELECT MAX(u.usedAt) FROM UsageLog u WHERE u.subscription = s) < :cutoff " +
           "OR (SELECT COUNT(u) FROM UsageLog u WHERE u.subscription = s) = 0)")
    List<Subscription> findAllUnused(@Param("cutoff") java.time.LocalDateTime cutoff);
}
