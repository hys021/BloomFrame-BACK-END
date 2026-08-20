package com.bloomframe.server.reminder.service;

import com.bloomframe.server.alarm.repository.CustomAlarmRepository;
import com.bloomframe.server.alarm.repository.ExerciseAlarmRepository;
import com.bloomframe.server.alarm.repository.MedicationAlarmRepository;
import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.reminder.dto.response.ReminderResponse;
import com.bloomframe.server.reminder.model.Reminder;
import com.bloomframe.server.reminder.repository.ReminderRepository;
import com.bloomframe.server.verification.model.ReminderSnapshot;
import com.bloomframe.server.verification.model.ReminderType;
import com.google.cloud.Timestamp;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final MedicationAlarmRepository medicationAlarmRepository;
    private final ExerciseAlarmRepository exerciseAlarmRepository;
    private final CustomAlarmRepository customAlarmRepository;
    private final ReminderFollowUpService reminderFollowUpService;

    public ReminderService(
            ReminderRepository reminderRepository,
            MedicationAlarmRepository medicationAlarmRepository,
            ExerciseAlarmRepository exerciseAlarmRepository,
            CustomAlarmRepository customAlarmRepository,
            ReminderFollowUpService reminderFollowUpService
    ) {
        this.reminderRepository = reminderRepository;
        this.medicationAlarmRepository = medicationAlarmRepository;
        this.exerciseAlarmRepository = exerciseAlarmRepository;
        this.customAlarmRepository = customAlarmRepository;
        this.reminderFollowUpService = reminderFollowUpService;
    }

    public List<ReminderResponse> getReminders(String userId) {
        return reminderRepository.findAllByUserId(userId)
                .stream()
                .map(ReminderResponse::from)
                .toList();
    }

    public ReminderResponse getReminder(String userId, String reminderId) {
        Reminder reminder = reminderRepository.findById(userId, reminderId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.REMINDER_NOT_FOUND));

        return ReminderResponse.from(reminder);
    }

    public void createScheduledReminders() {
        String currentTime = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        medicationAlarmRepository.findAllByAlarmTime(currentTime)
                .forEach(alarm ->
                        createReminder(
                                alarm.getUserId(),
                                "MEDICATION",
                                alarm.getId()
                        )
                );

        exerciseAlarmRepository.findAllByAlarmTime(currentTime)
                .forEach(alarm ->
                        createReminder(
                                alarm.getUserId(),
                                "EXERCISE",
                                alarm.getId()
                        )
                );

        customAlarmRepository.findAllByAlarmTime(currentTime)
                .forEach(alarm ->
                        createReminder(
                                alarm.getUserId(),
                                "CUSTOM",
                                alarm.getId()
                        )
                );
    }

    private void createReminder(
            String userId,
            String type,
            String targetId
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

        ZonedDateTime scheduledTime = now
                .withSecond(0)
                .withNano(0);

        Timestamp scheduledAt = Timestamp.ofTimeSecondsAndNanos(
                scheduledTime.toEpochSecond(),
                0
        );

        boolean alreadyExists =
                reminderRepository.existsByTargetIdAndScheduledAt(
                        userId,
                        targetId,
                        scheduledAt
                );

        if (alreadyExists) {
            return;
        }

        Reminder reminder = new Reminder(
                userId,
                type,
                targetId,
                scheduledAt,
                "PENDING"
        );

        Reminder saved = reminderRepository.save(reminder);

        reminderFollowUpService.scheduleFollowUps(
                userId,
                new ReminderSnapshot(
                        saved.getId(),
                        ReminderType.valueOf(type),
                        scheduledTime.toInstant()
                )
        );
    }
}