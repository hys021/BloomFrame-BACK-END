package com.bloomframe.server.flower.scheduler;

import com.bloomframe.server.flower.service.PlantStateService;
import com.bloomframe.server.verification.model.ReminderSnapshot;
import com.bloomframe.server.verification.repository.ReminderReader;
import com.bloomframe.server.verification.repository.UserDirectoryReader;
import com.bloomframe.server.verification.repository.VerificationLogRepository;
import com.bloomframe.server.verification.service.VerificationWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.time.Clock;
import java.time.Instant;

/**
 * scheduledAt + 3분이 지났는데도 아직 성공 인증이 없으면 식물을 시들게 한다.
 * verification의 MissedVerificationScheduler와 동일한 방식(전체 스캔)으로 동작 —
 * 시연 규모 기준. 유저가 많아지면 두 스케줄러가 중복으로 스캔한다는 점 인지하고 있음.
 */
@Component
public class WiltStartScheduler {

    private static final Logger log = LoggerFactory.getLogger(WiltStartScheduler.class);

    private final UserDirectoryReader userDirectoryReader;
    private final ReminderReader reminderReader;
    private final VerificationLogRepository logRepository;
    private final PlantStateService plantStateService;
    private final Clock clock;

    public WiltStartScheduler(UserDirectoryReader userDirectoryReader, ReminderReader reminderReader,
                              VerificationLogRepository logRepository, PlantStateService plantStateService,
                              Clock clock) {
        this.userDirectoryReader = userDirectoryReader;
        this.reminderReader = reminderReader;
        this.logRepository = logRepository;
        this.plantStateService = plantStateService;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 60_000)
    public void detectWiltStart() {
        Instant now = clock.instant();

        for (String uid : userDirectoryReader.findAllUids()) {
            try {
                processUser(uid, now);
            } catch (Exception e) {
                log.error("시듦 감지 처리 중 오류 발생: uid={}", uid, e);
            }
        }
    }

    private void processUser(String uid, Instant now) {
        List<ReminderSnapshot> reminders = reminderReader.findAllForUser(uid);

        // 이미 도래한(scheduledAt <= now) 알림 중 가장 최근 것만 판단 대상으로 삼는다.
        // 오래된 알림이 나중에 다시 wilted를 덮어쓰는 걸 막기 위함.
        ReminderSnapshot latestDue = reminders.stream()
                .filter(r -> !r.scheduledAt().isAfter(now))
                .max(Comparator.comparing(ReminderSnapshot::scheduledAt))
                .orElse(null);

        if (latestDue == null) {
            return; // 아직 도래한 알림이 없음
        }

        Instant wiltDeadline = latestDue.scheduledAt().plus(VerificationWindows.FIRST_STAGE_WINDOW);
        if (!now.isAfter(wiltDeadline)) {
            return; // 가장 최근 알림도 아직 3분 안 지남
        }
        if (logRepository.existsSuccessForReminderOccurrence(uid, latestDue.id(), latestDue.scheduledAt())) {
            return; // 가장 최근 알림은 이미 성공했음
        }

        plantStateService.markWilted(uid);
        log.info("시듦 시작: uid={}, reminderId={}, scheduledAt={}", uid, latestDue.id(), latestDue.scheduledAt());
    }
}