package com.bloomframe.server.verification;

/**
 * POST /api/v1/auth-touch 응답 바디로 그대로 나가는 결과 객체.
 */
public record AuthTouchResult(
        String logId,
        ReminderType type,
        AlertStage alertStage,
        VerificationStatus status
) {
}