package com.bloomframe.server.verification.model;

import java.time.Instant;

/**
 * users/{uid}/reminders/{id} 문서를 읽기 전용으로 조회한 스냅샷.
 * 이 문서의 실제 소유자는 Java #1이며, 아래 필드명은 아직 정식 스펙을 전달받기 전
 * Firestore 스키마 문서 기준의 가정
 *
 * TODO(Java #1 확정 후 수정): 실제 필드명이 다르면 FirestoreReminderReader에서
 * 파싱하는 키 이름만 바꾸면 되고, 이 record와 나머지 verification 로직은 그대로 둘 수 있게
 * 여기서 한번 감싸서 사용
 */
public record ReminderSnapshot(
        String id,
        ReminderType type,
        Instant scheduledAt
) {
}