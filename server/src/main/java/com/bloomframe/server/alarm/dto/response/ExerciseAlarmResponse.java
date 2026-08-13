package com.bloomframe.server.alarm.dto.response;

import java.time.LocalTime;

public record ExerciseAlarmResponse(
        String id,
        String exerciseName,
        LocalTime alarmTime
) {}
