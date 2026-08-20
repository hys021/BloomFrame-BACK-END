package com.bloomframe.server.alarm.service;

import com.bloomframe.server.alarm.dto.request.ExerciseAlarmRequest;
import com.bloomframe.server.alarm.dto.response.ExerciseAlarmResponse;
import com.bloomframe.server.alarm.model.ExerciseAlarm;
import com.bloomframe.server.alarm.repository.ExerciseAlarmRepository;
import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExerciseAlarmService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExerciseAlarmRepository exerciseAlarmRepository;

    public ExerciseAlarmService(ExerciseAlarmRepository exerciseAlarmRepository) {
        this.exerciseAlarmRepository = exerciseAlarmRepository;
    }

    public List<ExerciseAlarmResponse> getAlarms(String userId) {
        return exerciseAlarmRepository.findAllByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExerciseAlarmResponse register(String userId, ExerciseAlarmRequest request) {
        ExerciseAlarm alarm = new ExerciseAlarm(
                userId,
                request.exerciseName(),
                request.alarmTime().format(TIME_FORMAT),
                request.startDate().format(DATE_FORMAT)
        );

        exerciseAlarmRepository.save(alarm);
        return toResponse(alarm);
    }

    public ExerciseAlarmResponse update(
            String userId,
            String alarmId,
            ExerciseAlarmRequest request
    ) {
        ExerciseAlarm alarm = findOwned(userId, alarmId);

        alarm.setExerciseName(request.exerciseName());
        alarm.setAlarmTime(request.alarmTime().format(TIME_FORMAT));
        alarm.setStartDate(request.startDate().format(DATE_FORMAT));

        exerciseAlarmRepository.update(alarmId, alarm);
        return toResponse(alarm);
    }

    public void delete(String userId, String alarmId) {
        findOwned(userId, alarmId);
        exerciseAlarmRepository.deleteById(alarmId);
    }

    private ExerciseAlarm findOwned(String userId, String alarmId) {
        ExerciseAlarm alarm = exerciseAlarmRepository.findById(alarmId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.EXERCISE_ALARM_NOT_FOUND));

        if (!alarm.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.EXERCISE_ALARM_FORBIDDEN);
        }

        return alarm;
    }

    private ExerciseAlarmResponse toResponse(ExerciseAlarm alarm) {
        LocalDate startDate = alarm.getStartDate() == null
                ? null
                : LocalDate.parse(alarm.getStartDate(), DATE_FORMAT);

        return new ExerciseAlarmResponse(
                alarm.getId(),
                alarm.getExerciseName(),
                LocalTime.parse(alarm.getAlarmTime(), TIME_FORMAT),
                startDate
        );
    }
}