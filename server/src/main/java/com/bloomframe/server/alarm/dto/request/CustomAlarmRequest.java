package com.bloomframe.server.alarm.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CustomAlarmRequest(
        String title,
        @NotNull LocalTime alarmTime,
        @NotNull LocalDate startDate
) {}