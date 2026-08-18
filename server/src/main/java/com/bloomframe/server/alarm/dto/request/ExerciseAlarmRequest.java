package com.bloomframe.server.alarm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ExerciseAlarmRequest(
        String exerciseName,
        @NotNull LocalTime alarmTime
) {}
