package com.bloomframe.server.verification.model;

/**
 * 알림 카테고리. Java #1의 reminders 문서에도 동일한 값이 존재한다고 가정한다.
 * (필드명이 다르게 확정되면 이 enum 값 매핑만 수정하면 됨)
 */
public enum ReminderType {
    MEDICATION,
    EXERCISE,
    CUSTOM
}