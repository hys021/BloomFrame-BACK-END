package com.bloomframe.server.alarm.service;

import com.bloomframe.server.alarm.dto.request.CustomAlarmRequest;
import com.bloomframe.server.alarm.dto.response.CustomAlarmResponse;
import com.bloomframe.server.alarm.model.CustomAlarm;
import com.bloomframe.server.alarm.repository.CustomAlarmRepository;
import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CustomAlarmService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final CustomAlarmRepository customAlarmRepository;

    public CustomAlarmService(CustomAlarmRepository customAlarmRepository) {
        this.customAlarmRepository = customAlarmRepository;
    }

    public List<CustomAlarmResponse> getAlarms(String userId) {
        return customAlarmRepository.findAllByUserIdOrderByAlarmTime(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomAlarmResponse register(String userId, CustomAlarmRequest request) {
        CustomAlarm alarm = new CustomAlarm(userId, request.title(), request.alarmTime().format(TIME_FORMAT));
        customAlarmRepository.save(alarm);
        return toResponse(alarm);
    }

    public CustomAlarmResponse update(String userId, String alarmId, CustomAlarmRequest request) {
        CustomAlarm alarm = findOwned(userId, alarmId);
        alarm.setTitle(request.title());
        alarm.setAlarmTime(request.alarmTime().format(TIME_FORMAT));
        customAlarmRepository.update(alarmId, alarm);
        return toResponse(alarm);
    }

    public void delete(String userId, String alarmId) {
        findOwned(userId, alarmId);
        customAlarmRepository.deleteById(alarmId);
    }

    private CustomAlarm findOwned(String userId, String alarmId) {
        CustomAlarm alarm = customAlarmRepository.findById(alarmId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOM_ALARM_NOT_FOUND));

        if (!alarm.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.CUSTOM_ALARM_FORBIDDEN);
        }
        return alarm;
    }

    private CustomAlarmResponse toResponse(CustomAlarm alarm) {
        return new CustomAlarmResponse(alarm.getId(), alarm.getTitle(), LocalTime.parse(alarm.getAlarmTime(), TIME_FORMAT));
    }
}
