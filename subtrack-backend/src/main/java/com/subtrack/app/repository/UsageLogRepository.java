package com.subtrack.app.repository;

import com.subtrack.app.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, UUID> {

    List<UsageLog> findBySubscriptionIdOrderByUsedAtDesc(UUID subscriptionId);

    List<UsageLog> findByUserIdOrderByUsedAtDesc(UUID userId);

    @Query("SELECT MAX(u.usedAt) FROM UsageLog u WHERE u.subscription.id = :subscriptionId")
    Optional<LocalDateTime> findLastUsedAt(@Param("subscriptionId") UUID subscriptionId);

    long countBySubscriptionId(UUID subscriptionId);

    @Query("SELECT COUNT(u) FROM UsageLog u WHERE u.subscription.id = :subscriptionId " +
           "AND u.usedAt >= :since")
    long countRecentUsage(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("since")          LocalDateTime since);
}
