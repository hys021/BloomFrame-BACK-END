package com.bloomframe.server.alarm.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record CustomAlarmResponse(
        String id,
        String title,
        LocalTime alarmTime,
        LocalDate startDate
) {}