package com.bloomframe.server.verification.scheduler;

import com.bloomframe.server.verification.model.AlertStage;
import com.bloomframe.server.verification.model.ReminderSnapshot;
import com.bloomframe.server.verification.model.VerificationLog;
import com.bloomframe.server.verification.repository.ReminderReader;
import com.bloomframe.server.verification.repository.UserDirectoryReader;
import com.bloomframe.server.verification.repository.VerificationLogRepository;
import com.bloomframe.server.verification.service.VerificationWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * scheduledAt + 10분이 지났는데도 인증 로그가 없는 reminder를 찾아 MISSED로 확정한다.
 * 시연/해커톤 규모 기준 — 전체 유저를 매 주기마다 스캔하는 단순한 방식.
 */
@Component
public class MissedVerificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(MissedVerificationScheduler.class);

    private final UserDirectoryReader userDirectoryReader;
    private final ReminderReader reminderReader;
    private final VerificationLogRepository logRepository;
    private final Clock clock;

    public MissedVerificationScheduler(UserDirectoryReader userDirectoryReader, ReminderReader reminderReader,
                                       VerificationLogRepository logRepository, Clock clock) {
        this.userDirectoryReader = userDirectoryReader;
        this.reminderReader = reminderReader;
        this.logRepository = logRepository;
        this.clock = clock;
    }

    @Scheduled(fixedRate = 60_000)
    public void detectMissed() {
        Instant now = clock.instant();

        for (String uid : userDirectoryReader.findAllUids()) {
            try {
                processUser(uid, now);
            } catch (Exception e) {
                // 한 유저 처리 중 오류가 나도 나머지 유저 처리는 계속 진행
                log.error("미인증 감지 처리 중 오류 발생: uid={}", uid, e);
            }
        }
    }

    private void processUser(String uid, Instant now) {
        Instant since = now.minus(VerificationWindows.SCHEDULER_LOOKBACK);
        for (ReminderSnapshot reminder : reminderReader.findRecentForUser(uid, since)) {
            Instant finalDeadline = reminder.scheduledAt().plus(VerificationWindows.FINAL_WINDOW);
            if (!now.isAfter(finalDeadline)) {
                continue; // 아직 10분 안 지남
            }
            if (logRepository.existsForReminderOccurrence(uid, reminder.id(), reminder.scheduledAt())) {
                continue; // 이미 성공했거나 이미 MISSED 처리됨
            }

            VerificationLog missed = VerificationLog.missed(reminder.type(), reminder.id(), AlertStage.SECOND, reminder.scheduledAt());
            logRepository.save(uid, missed);
            log.info("MISSED 확정: uid={}, reminderId={}, scheduledAt={}", uid, reminder.id(), reminder.scheduledAt());
        }
    }
}