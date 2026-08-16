package com.bloomframe.server.flower.model;

/**
 * 식물 상태는 이원화 — 다단계 성장이 아니라 BLOOMING/WILTED만 오간다.
 */
public enum PlantStatus {
    BLOOMING,
    WILTED
}