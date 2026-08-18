package com.bloomframe.server.alarm.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

// medication-alarms 전용 (exercise/custom은 필드가 달라 별도 DTO 사용)
public record AlarmRequest(
        @NotNull LocalTime alarmTime
) {}
