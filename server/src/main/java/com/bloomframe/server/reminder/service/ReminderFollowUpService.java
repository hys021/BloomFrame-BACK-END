package com.bloomframe.server.reminder.service;

import com.bloomframe.server.flower.service.PlantStateService;
import com.bloomframe.server.verification.model.AlertStage;
import com.bloomframe.server.verification.model.ReminderSnapshot;
import com.bloomframe.server.verification.model.ReminderType;
import com.bloomframe.server.verification.model.VerificationLog;
import com.bloomframe.server.verification.repository.ReminderReader;
import com.bloomframe.server.verification.repository.UserDirectoryReader;
import com.bloomframe.server.verification.repository.VerificationLogRepository;
import com.bloomframe.server.verification.service.VerificationWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Reminder 생성 시점에 Wilt(T+3) / MISSED(T+10) 검사를 예약한다.
 * 매분 전체 유저 스캔 대신 occurrence 단위로 Firestore read를 줄인다.
 */
@Service
public class ReminderFollowUpService {

    private static final Logger log = LoggerFactory.getLogger(ReminderFollowUpService.class);
    private static final int MAX_RETRIES = 2;
    private static final Duration RETRY_DELAY = Duration.ofMinutes(2);
    /** 마감 시각에 아직 isAfter가 아닐 때, 원본 매분 스캔처럼 1분 뒤 1회만 다시 본다. */
    private static final Duration EXTRA_CHECK_DELAY = Duration.ofMinutes(1);

    @FunctionalInterface
    private interface FollowUpEval {
        /** @return true면 마감이 아직 안 지남 — 1분 뒤 추가 확인 대상 */
        boolean run();
    }

    private final TaskScheduler taskScheduler;
    private final UserDirectoryReader userDirectoryReader;
    private final ReminderReader reminderReader;
    private final VerificationLogRepository logRepository;
    private final PlantStateService plantStateService;
    private final Clock clock;

    public ReminderFollowUpService(
            @Qualifier("reminderFollowUpTaskScheduler") TaskScheduler reminderFollowUpTaskScheduler,
            UserDirectoryReader userDirectoryReader,
            ReminderReader reminderReader,
            VerificationLogRepository logRepository,
            PlantStateService plantStateService,
            Clock clock
    ) {
        this.taskScheduler = reminderFollowUpTaskScheduler;
        this.userDirectoryReader = userDirectoryReader;
        this.reminderReader = reminderReader;
        this.logRepository = logRepository;
        this.plantStateService = plantStateService;
        this.clock = clock;
    }

    /**
     * 새 Reminder 저장 직후 호출 — Wilt / MISSED 검사를 각각 T+3, T+10에 예약한다.
     * 마감 시각에 아직 확정할 수 없으면 1분 뒤 1회만 추가로 확인한다.
     */
    public void scheduleFollowUps(String uid, ReminderSnapshot reminder) {
        Instant now = clock.instant();
        Instant wiltAt = reminder.scheduledAt().plus(VerificationWindows.FIRST_STAGE_WINDOW);
        Instant missedAt = reminder.scheduledAt().plus(VerificationWindows.FINAL_WINDOW);

        scheduleWithRetry("wilt", uid, () -> evaluateWilt(uid), wiltAt, now);
        scheduleWithRetry("missed", uid, () -> evaluateMissed(uid, reminder), missedAt, now);
    }

    /**
     * 재시작 직후 12분 lookback — 놓친 Wilt / MISSED를 한 번 따라잡는다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void catchUpAfterStartup() {
        Instant now = clock.instant();
        Instant since = now.minus(VerificationWindows.SCHEDULER_LOOKBACK);

        log.info("Reminder follow-up startup catch-up (since={})", since);

        for (String uid : userDirectoryReader.findAllUids()) {
            try {
                List<ReminderSnapshot> reminders = reminderReader.findRecentForUser(uid, since);
                for (ReminderSnapshot reminder : reminders) {
                    scheduleFollowUps(uid, reminder);
                }
            } catch (Exception e) {
                log.error("Startup catch-up failed: uid={}", uid, e);
            }
        }
    }

    private void scheduleWithRetry(String kind, String uid, FollowUpEval task, Instant runAt, Instant now) {
        if (!runAt.isAfter(now)) {
            runWithRetry(kind, uid, task, 0, true);
            return;
        }
        taskScheduler.schedule(() -> runWithRetry(kind, uid, task, 0, true), runAt);
    }

    private void runWithRetry(String kind, String uid, FollowUpEval task, int attempt, boolean extraCheckAllowed) {
        try {
            boolean needsExtraCheck = task.run();
            if (needsExtraCheck && extraCheckAllowed) {
                Instant extraAt = clock.instant().plus(EXTRA_CHECK_DELAY);
                taskScheduler.schedule(() -> runWithRetry(kind, uid, task, 0, false), extraAt);
            }
        } catch (Exception e) {
            log.warn("Reminder follow-up {} failed: uid={}, attempt={}", kind, uid, attempt, e);
            if (attempt < MAX_RETRIES) {
                Instant retryAt = clock.instant().plus(RETRY_DELAY);
                taskScheduler.schedule(
                        () -> runWithRetry(kind, uid, task, attempt + 1, extraCheckAllowed),
                        retryAt
                );
            }
        }
    }

    /** WiltStartScheduler.processUser 와 동일 — 최근 도래 알림 1건만 판단. */
    boolean evaluateWilt(String uid) {
        Instant now = clock.instant();
        Instant since = now.minus(VerificationWindows.SCHEDULER_LOOKBACK);
        List<ReminderSnapshot> reminders = reminderReader.findRecentForUser(uid, since);

        ReminderSnapshot latestDue = reminders.stream()
                .filter(r -> !r.scheduledAt().isAfter(now))
                .max(Comparator.comparing(ReminderSnapshot::scheduledAt))
                .orElse(null);

        if (latestDue == null) {
            return false;
        }

        Instant wiltDeadline = latestDue.scheduledAt().plus(VerificationWindows.FIRST_STAGE_WINDOW);
        if (!now.isAfter(wiltDeadline)) {
            return true;
        }
        if (logRepository.existsSuccessForReminderOccurrence(uid, latestDue.id(), latestDue.scheduledAt())) {
            return false;
        }

        plantStateService.markWilted(uid);
        log.info("시듦 시작: uid={}, reminderId={}, scheduledAt={}", uid, latestDue.id(), latestDue.scheduledAt());
        return false;
    }

    /** MissedVerificationScheduler.processUser 내 occurrence 1건과 동일. */
    boolean evaluateMissed(String uid, ReminderSnapshot reminder) {
        Instant now = clock.instant();
        Instant finalDeadline = reminder.scheduledAt().plus(VerificationWindows.FINAL_WINDOW);
        if (!now.isAfter(finalDeadline)) {
            return true;
        }
        if (logRepository.existsForReminderOccurrence(uid, reminder.id(), reminder.scheduledAt())) {
            return false;
        }

        VerificationLog missed = VerificationLog.missed(
                reminder.type(), reminder.id(), AlertStage.SECOND, reminder.scheduledAt());
        logRepository.save(uid, missed);
        log.info("MISSED 확정: uid={}, reminderId={}, scheduledAt={}", uid, reminder.id(), reminder.scheduledAt());
        return false;
    }
}
