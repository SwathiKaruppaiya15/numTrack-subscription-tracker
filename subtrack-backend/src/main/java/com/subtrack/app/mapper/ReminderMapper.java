package com.subtrack.app.mapper;

import com.subtrack.app.dto.response.ReminderResponse;
import com.subtrack.app.entity.Reminder;
import org.springframework.stereotype.Component;

@Component
public class ReminderMapper {

    public ReminderResponse toResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .subscriptionId(reminder.getSubscription().getId())
                .subscriptionName(reminder.getSubscription().getName())
                .subscriptionAmount(reminder.getSubscription().getAmount())
                .reminderType(reminder.getReminderType())
                .scheduledFor(reminder.getScheduledFor())
                .sentAt(reminder.getSentAt())
                .sent(reminder.getSentAt() != null)
                .build();
    }
}
