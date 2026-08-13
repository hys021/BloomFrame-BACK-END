package com.bloomframe.server.verification;

import java.time.Instant;

/**
 * users/{uid}/verificationLogs/{logId} 문서에 대응하는 도메인 모델.
 *
 * @param targetId   reminders 컬렉션 문서 참조 id
 * @param verifiedAt 인증 성공 시각. MISSED 상태에서는 null.
 */
public record VerificationLog(
        String id,
        ReminderType type,
        String targetId,
        AlertStage alertStage,
        VerificationStatus status,
        Instant scheduledAt,
        Instant verifiedAt
) {
    public static VerificationLog success(ReminderType type, String targetId, AlertStage alertStage,
                                          Instant scheduledAt, Instant verifiedAt) {
        return new VerificationLog(null, type, targetId, alertStage, VerificationStatus.SUCCESS, scheduledAt, verifiedAt);
    }

    public static VerificationLog missed(ReminderType type, String targetId, AlertStage alertStage, Instant scheduledAt) {
        return new VerificationLog(null, type, targetId, alertStage, VerificationStatus.MISSED, scheduledAt, null);
    }
}