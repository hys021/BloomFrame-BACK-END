package com.bloomframe.server.flower.model;

import java.time.Instant;

/**
 * users/{uid}/plantState 문서에 대응하는 도메인 모델.
 */
public record PlantState(
        PlantStatus status,
        Instant updatedAt
) {
    /** 문서가 아직 없는 신규 유저 기본값 — 첫 알림 전까지는 blooming으로 취급. */
    public static PlantState defaultState(Instant now) {
        return new PlantState(PlantStatus.BLOOMING, now);
    }
}