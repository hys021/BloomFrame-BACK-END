package com.bloomframe.server.reminder.service;

import com.bloomframe.server.flower.service.PlantStateService;
import com.bloomframe.server.verification.model.ReminderSnapshot;
import com.bloomframe.server.verification.model.ReminderType;
import com.bloomframe.server.verification.model.VerificationLog;
import com.bloomframe.server.verification.repository.ReminderReader;
import com.bloomframe.server.verification.repository.UserDirectoryReader;
import com.bloomframe.server.verification.repository.VerificationLogRepository;
import com.bloomframe.server.verification.service.VerificationWindows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderFollowUpServiceTest {

    private static final Instant BASE = Instant.parse("2026-08-20T08:00:00Z");
    private static final String UID = "user1";

    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private UserDirectoryReader userDirectoryReader;
    @Mock
    private ReminderReader reminderReader;
    @Mock
    private VerificationLogRepository logRepository;
    @Mock
    private PlantStateService plantStateService;

    private ReminderFollowUpService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(BASE, ZoneId.of("UTC"));
        service = new ReminderFollowUpService(
                taskScheduler,
                userDirectoryReader,
                reminderReader,
                logRepository,
                plantStateService,
                clock
        );
    }

    @Test
    void evaluateMissed_skipsBeforeDeadline() {
        ReminderSnapshot reminder = new ReminderSnapshot("r1", ReminderType.MEDICATION, BASE);

        service.evaluateMissed(UID, reminder);

        verify(logRepository, never()).save(eq(UID), any());
    }

    @Test
    void evaluateMissed_recordsWhenPastDeadlineAndNoLog() {
        clock = Clock.fixed(BASE.plusSeconds(601), ZoneId.of("UTC"));
        service = new ReminderFollowUpService(
                taskScheduler, userDirectoryReader, reminderReader, logRepository, plantStateService, clock);

        ReminderSnapshot reminder = new ReminderSnapshot("r1", ReminderType.MEDICATION, BASE);
        when(logRepository.existsForReminderOccurrence(UID, "r1", BASE)).thenReturn(false);

        service.evaluateMissed(UID, reminder);

        verify(logRepository).save(eq(UID), any(VerificationLog.class));
    }

    @Test
    void evaluateWilt_usesLatestDueReminder() {
        clock = Clock.fixed(BASE.plusSeconds(200), ZoneId.of("UTC"));
        service = new ReminderFollowUpService(
                taskScheduler, userDirectoryReader, reminderReader, logRepository, plantStateService, clock);

        ReminderSnapshot older = new ReminderSnapshot("r1", ReminderType.MEDICATION, BASE.minusSeconds(3600));
        ReminderSnapshot latest = new ReminderSnapshot("r2", ReminderType.MEDICATION, BASE.minusSeconds(200));
        when(reminderReader.findRecentForUser(eq(UID), any())).thenReturn(List.of(older, latest));
        when(logRepository.existsSuccessForReminderOccurrence(UID, "r2", latest.scheduledAt())).thenReturn(false);

        service.evaluateWilt(UID);

        verify(plantStateService).markWilted(UID);
    }

    @Test
    void evaluateWilt_atExactDeadline_doesNotMark() {
        Instant wiltAt = BASE.plus(VerificationWindows.FIRST_STAGE_WINDOW);
        clock = Clock.fixed(wiltAt, ZoneId.of("UTC"));
        service = new ReminderFollowUpService(
                taskScheduler, userDirectoryReader, reminderReader, logRepository, plantStateService, clock);

        ReminderSnapshot reminder = new ReminderSnapshot("r1", ReminderType.MEDICATION, BASE);
        when(reminderReader.findRecentForUser(eq(UID), any())).thenReturn(List.of(reminder));

        assertEquals(true, service.evaluateWilt(UID));
        verify(plantStateService, never()).markWilted(UID);
    }

    @Test
    void scheduleFollowUps_whenDeadlineNotPassed_schedulesOneExtraCheckThenStops() {
        Instant wiltAt = BASE.plus(VerificationWindows.FIRST_STAGE_WINDOW);
        clock = Clock.fixed(wiltAt, ZoneId.of("UTC"));
        service = new ReminderFollowUpService(
                taskScheduler, userDirectoryReader, reminderReader, logRepository, plantStateService, clock);

        ReminderSnapshot reminder = new ReminderSnapshot("r1", ReminderType.MEDICATION, BASE);
        when(reminderReader.findRecentForUser(eq(UID), any())).thenReturn(List.of(reminder));

        service.scheduleFollowUps(UID, reminder);

        Instant extraAt = wiltAt.plus(Duration.ofMinutes(1));
        Instant missedAt = BASE.plus(VerificationWindows.FINAL_WINDOW);

        ArgumentCaptor<Runnable> runnables = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Instant> when = ArgumentCaptor.forClass(Instant.class);
        verify(taskScheduler, times(2)).schedule(runnables.capture(), when.capture());
        assertEquals(List.of(extraAt, missedAt), when.getAllValues());

        runnables.getAllValues().get(0).run();

        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
        verify(plantStateService, never()).markWilted(UID);
    }
}
