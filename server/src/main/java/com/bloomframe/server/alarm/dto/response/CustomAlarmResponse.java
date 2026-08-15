package com.bloomframe.server.alarm.dto.response;

import java.time.LocalTime;

public record CustomAlarmResponse(
        String id,
        String title,
        LocalTime alarmTime
) {}
