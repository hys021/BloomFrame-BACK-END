package com.bloomframe.server.verification;

import java.time.Instant;
import java.util.List;

public interface VerificationLogRepository {

    /** 로그를 저장하고 생성된 문서 id를 반환한다. */
    String save(String uid, VerificationLog log);

    /** 홈탭 캘린더 조회용 — scheduledAt 기준 [from, to] 범위. */
    List<VerificationLog> findByScheduledAtRange(String uid, Instant from, Instant to);
}