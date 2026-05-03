package com.subtrack.app.repository;

import com.subtrack.app.entity.Reminder;
import com.subtrack.app.entity.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByUserIdOrderByScheduledForDesc(UUID userId);

    // Check if reminder already exists (idempotency guard)
    boolean existsBySubscriptionIdAndReminderTypeAndScheduledFor(
            UUID subscriptionId, ReminderType reminderType, LocalDate scheduledFor);

    // Unsent reminders due today (for scheduler)
    @Query("SELECT r FROM Reminder r WHERE r.sentAt IS NULL AND r.scheduledFor <= :today")
    List<Reminder> findUnsentDueReminders(@Param("today") LocalDate today);

    // All reminders for a subscription
    List<Reminder> findBySubscriptionIdOrderByScheduledForDesc(UUID subscriptionId);

    // Sent reminders for a user
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.sentAt IS NOT NULL " +
           "ORDER BY r.sentAt DESC")
    List<Reminder> findSentByUser(@Param("userId") UUID userId);
}
