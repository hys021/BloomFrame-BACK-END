package com.bloomframe.server.alarm.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record AlarmResponse(
        String id,
        LocalTime alarmTime,
        LocalDate startDate
) {}