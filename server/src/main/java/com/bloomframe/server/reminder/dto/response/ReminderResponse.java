package com.bloomframe.server.reminder.dto.response;

import com.bloomframe.server.reminder.model.Reminder;
import com.google.cloud.Timestamp;

public record ReminderResponse(
        String id,
        String type,
        String targetId,
        Timestamp scheduledAt,
        String status
) {

    public static ReminderResponse from(Reminder reminder) {
        return new ReminderResponse(
                reminder.getId(),
                reminder.getType(),
                reminder.getTargetId(),
                reminder.getScheduledAt(),
                reminder.getStatus()
        );
    }
}