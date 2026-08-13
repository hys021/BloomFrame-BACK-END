package com.bloomframe.server.alarm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ExerciseAlarmRequest(
        @NotBlank(message = "운동 이름은 필수입니다.") String exerciseName,
        @NotNull LocalTime alarmTime
) {}
