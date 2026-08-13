package com.bloomframe.server.verification;

import java.util.Optional;

/**
 * users/{uid}/reminders/{id} 읽기 전용 접근.
 * verification 패키지는 이 컬렉션을 절대 쓰기(write)하지 않는다 — 소유권은 Java #1.
 */
public interface ReminderReader {
    Optional<ReminderSnapshot> findById(String uid, String reminderId);
}