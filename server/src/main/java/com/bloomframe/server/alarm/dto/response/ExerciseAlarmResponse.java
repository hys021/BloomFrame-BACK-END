package com.bloomframe.server.alarm.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExerciseAlarmResponse(
        String id,
        String exerciseName,
        LocalTime alarmTime,
        LocalDate startDate
) {}