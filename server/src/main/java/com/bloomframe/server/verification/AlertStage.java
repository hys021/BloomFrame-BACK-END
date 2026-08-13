package com.bloomframe.server.verification;

/**
 * 인증이 몇 차 알림 단계에서 일어났는지 나타내는 메타데이터.
 * 결과(성공/실패) 판정과는 무관하다 — 1차든 2차든 인증 성공은 동일하게 SUCCESS로 기록된다.
 */
public enum AlertStage {
    FIRST(1),
    SECOND(2);

    private final int order;

    AlertStage(int order) {
        this.order = order;
    }

    public int order() {
        return order;
    }
}