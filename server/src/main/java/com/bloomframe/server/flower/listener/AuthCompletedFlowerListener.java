package com.bloomframe.server.flower.listener;

import com.bloomframe.server.flower.service.PlantStateService;
import com.bloomframe.server.verification.event.AuthCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * AuthTouchService가 인증 성공 시 발행하는 AuthCompletedEvent를 구독해서
 * 식물을 blooming으로 되돌린다. (verification 코드를 직접 건드리지 않고 이벤트로만 연결)
 */
@Component
public class AuthCompletedFlowerListener {

    private static final Logger log = LoggerFactory.getLogger(AuthCompletedFlowerListener.class);

    private final PlantStateService plantStateService;

    public AuthCompletedFlowerListener(PlantStateService plantStateService) {
        this.plantStateService = plantStateService;
    }

    @Async
    @EventListener
    public void onAuthCompleted(AuthCompletedEvent event) {
        try {
            plantStateService.markBlooming(event.uid());
        } catch (Exception e) {
            log.error("blooming 처리 실패: uid={}, reminderId={}", event.uid(), event.reminderId(), e);
        }
    }
}