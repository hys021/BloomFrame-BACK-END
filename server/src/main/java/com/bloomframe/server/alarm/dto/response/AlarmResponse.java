package com.bloomframe.server.alarm.dto.response;

import java.time.LocalTime;

public record AlarmResponse(
        String id,
        LocalTime alarmTime
) {}
