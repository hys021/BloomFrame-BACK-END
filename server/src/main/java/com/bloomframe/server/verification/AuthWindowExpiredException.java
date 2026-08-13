package com.bloomframe.server.verification;

import java.time.Instant;

/**
 * scheduledAt + 10분이 지난 뒤에 인증 요청이 들어온 경우.
 * 정상 흐름이라면 이 시점엔 이미 스케줄러가 MISSED로 확정했어야 한다.
 */
public class AuthWindowExpiredException extends RuntimeException {
    public AuthWindowExpiredException(String reminderId, Instant scheduledAt) {
        super("auth window expired for reminder " + reminderId + " (scheduledAt=" + scheduledAt + ")");
    }
}