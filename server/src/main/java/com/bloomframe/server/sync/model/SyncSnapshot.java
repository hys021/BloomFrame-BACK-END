package com.bloomframe.server.sync.model;

import com.bloomframe.server.flower.model.PlantState;
import com.bloomframe.server.verification.model.VerificationLog;

import java.util.List;

/**
 * 폴링용 통합 스냅샷. 앱/탭이 이거 하나만 몇 초마다 호출하면
 * 꽃 상태 + 최근 인증 기록을 한 번에 받을 수 있다.
 */
public record SyncSnapshot(
        PlantState plantState,
        List<VerificationLog> recentLogs
) {
}