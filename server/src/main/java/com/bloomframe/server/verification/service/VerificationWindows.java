package com.bloomframe.server.verification.service;

import java.time.Duration;

/**
 * 알림 단계 판정 기준값. AuthTouchService(실시간 인증)와 MissedVerificationScheduler(사후 판정)가
 * 반드시 같은 값을 참조해야 하므로 한 곳에 모아둔다 — 따로 하드코딩하면 나중에 한쪽만 수정해서
 * 어긋나는 버그가 생기기 쉽다.
 */
public final class VerificationWindows {

    public static final Duration FIRST_STAGE_WINDOW = Duration.ofMinutes(3);
    public static final Duration FINAL_WINDOW = Duration.ofMinutes(10);

    /** 스케줄러가 reminder를 조회할 때 볼 최대 과거 범위. FINAL_WINDOW(10분)보다
     *  여유를 둬서, 경계 시점(정확히 10분째)에 놓치는 일이 없게 함. */
    public static final Duration SCHEDULER_LOOKBACK = FINAL_WINDOW.plusMinutes(2);

    private VerificationWindows() {
    }
}