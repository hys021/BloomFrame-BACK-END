package com.bloomframe.server.ai.dto;

import java.time.Instant;
import java.util.List;

public record NewsletterDto(
        String id,
        String uid,
        String trigger,
        String reminderId,
        String alarmId,
        String kind,
        String title,
        String body,
        List<String> tips,
        String status,
        Instant scheduledAt,
        Instant sentAt
) {
    public NewsletterDto withId(String newId) {
        return new NewsletterDto(
                newId, uid, trigger, reminderId, alarmId, kind, title, body, tips, status, scheduledAt, sentAt);
    }

    public NewsletterDto withKindAndContent(String newKind, String newTitle, String newBody, List<String> newTips) {
        return new NewsletterDto(
                id, uid, trigger, reminderId, alarmId, newKind, newTitle, newBody, newTips, status, scheduledAt, sentAt);
    }

    public NewsletterDto withSent(Instant when) {
        return new NewsletterDto(
                id, uid, trigger, reminderId, alarmId, kind, title, body, tips, "sent", scheduledAt, when);
    }

    public NewsletterDto withFailed() {
        return new NewsletterDto(
                id, uid, trigger, reminderId, alarmId, kind, title, body, tips, "failed", scheduledAt, sentAt);
    }
}
