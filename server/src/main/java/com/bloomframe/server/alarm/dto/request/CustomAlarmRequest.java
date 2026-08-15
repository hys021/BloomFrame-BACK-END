package com.bloomframe.server.alarm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CustomAlarmRequest(
        @NotBlank(message = "알림 항목 이름은 필수입니다.") String title,
        @NotNull LocalTime alarmTime
) {}
