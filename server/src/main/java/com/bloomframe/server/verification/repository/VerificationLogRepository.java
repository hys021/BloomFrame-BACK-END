package com.bloomframe.server.verification.repository;

import com.bloomframe.server.verification.model.VerificationLog;

import java.time.Instant;
import java.util.List;

public interface VerificationLogRepository {

    /** 로그를 저장하고 생성된 문서 id를 반환한다. */
    String save(String uid, VerificationLog log);

    /** 홈탭 캘린더 조회용 — scheduledAt 기준 [from, to] 범위. */
    List<VerificationLog> findByScheduledAtRange(String uid, Instant from, Instant to);

    /** 특정 reminder 발생(targetId + scheduledAt)에 대해 이미 로그(성공/실패 무관)가 있는지 확인.
     *  스케줄러의 중복 MISSED 생성을 막기 위해 사용. */
    boolean existsForReminderOccurrence(String uid, String targetId, Instant scheduledAt);

    /** 특정 reminder 발생에 대해 "성공" 로그가 있는지만 확인 (MISSED는 무시).
     *  flower 패키지의 시듦 트리거용 — MISSED가 생겨도 식물은 계속 시든 채로 둬야 하므로
     *  existsForReminderOccurrence(성공/실패 무관)와는 다르게 성공 여부만 본다. */
    boolean existsSuccessForReminderOccurrence(String uid, String targetId, Instant scheduledAt);
}