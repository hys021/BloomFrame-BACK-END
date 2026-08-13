package com.bloomframe.server.verification.service;

import com.bloomframe.server.verification.exception.AuthWindowExpiredException;
import com.bloomframe.server.verification.exception.ReminderNotFoundException;
import com.bloomframe.server.verification.model.*;
import com.bloomframe.server.verification.repository.ReminderReader;
import com.bloomframe.server.verification.repository.VerificationLogRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 터치 인증 처리 핵심 로직.
 *
 * 보안 원칙: 클라이언트는 reminderId만 보낸다. alertStage와 scheduledAt은
 * 절대 클라이언트 입력을 신뢰하지 않고, 서버가 reminder를 직접 조회해서 스스로 판단한다.
 */
@Service
public class AuthTouchService {

    private static final Duration FIRST_STAGE_WINDOW = Duration.ofMinutes(3);
    private static final Duration FINAL_WINDOW = Duration.ofMinutes(10);

    private final ReminderReader reminderReader;
    private final VerificationLogRepository logRepository;
    private final Clock clock;

    public AuthTouchService(ReminderReader reminderReader, VerificationLogRepository logRepository, Clock clock) {
        this.reminderReader = reminderReader;
        this.logRepository = logRepository;
        this.clock = clock;
    }

    public AuthTouchResult touch(String uid, String reminderId) {
        ReminderSnapshot reminder = reminderReader.findById(uid, reminderId)
                .orElseThrow(() -> new ReminderNotFoundException(reminderId));

        Instant now = clock.instant();
        Instant scheduledAt = reminder.scheduledAt();
        Instant firstDeadline = scheduledAt.plus(FIRST_STAGE_WINDOW);
        Instant finalDeadline = scheduledAt.plus(FINAL_WINDOW);

        if (now.isAfter(finalDeadline)) {
            throw new AuthWindowExpiredException(reminderId, scheduledAt);
        }

        AlertStage alertStage = now.isAfter(firstDeadline) ? AlertStage.SECOND : AlertStage.FIRST;

        VerificationLog log = VerificationLog.success(reminder.type(), reminder.id(), alertStage, scheduledAt, now);
        String logId = logRepository.save(uid, log);

        return new AuthTouchResult(logId, reminder.type(), alertStage, VerificationStatus.SUCCESS);
    }

    public List<VerificationLog> getLogs(String uid, Instant from, Instant to) {
        return logRepository.findByScheduledAtRange(uid, from, to);
    }
}