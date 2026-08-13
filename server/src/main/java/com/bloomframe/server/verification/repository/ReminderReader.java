package com.bloomframe.server.verification.repository;

import com.bloomframe.server.verification.model.ReminderSnapshot;

import java.util.List;
import java.util.Optional;

/**
 * users/{uid}/reminders/{id} 읽기 전용 접근.
 * verification 패키지는 이 컬렉션을 절대 쓰기(write)하지 않는다 — 소유권은 Java #1.
 */
public interface ReminderReader {
    Optional<ReminderSnapshot> findById(String uid, String reminderId);

    /** 미인증 감지 스케줄러용 — 한 유저의 모든 reminder를 가져온다. */
    List<ReminderSnapshot> findAllForUser(String uid);
}