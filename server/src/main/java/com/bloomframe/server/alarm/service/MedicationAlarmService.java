package com.bloomframe.server.alarm.service;

import com.bloomframe.server.alarm.dto.request.AlarmRequest;
import com.bloomframe.server.alarm.dto.response.AlarmResponse;
import com.bloomframe.server.alarm.model.MedicationAlarm;
import com.bloomframe.server.alarm.repository.MedicationAlarmRepository;
import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MedicationAlarmService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final MedicationAlarmRepository medicationAlarmRepository;

    public MedicationAlarmService(MedicationAlarmRepository medicationAlarmRepository) {
        this.medicationAlarmRepository = medicationAlarmRepository;
    }

    // 시간순으로 반환 -> "1회차/2회차..."는 프론트에서 배열 index로 계산 (sequence 컬럼 없음)
    public List<AlarmResponse> getAlarms(String userId) {
        return medicationAlarmRepository.findAllByUserIdOrderByAlarmTime(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public AlarmResponse register(String userId, AlarmRequest request) {
        MedicationAlarm alarm = new MedicationAlarm(userId, request.alarmTime().format(TIME_FORMAT));
        medicationAlarmRepository.save(alarm);
        return toResponse(alarm);
    }

    public AlarmResponse update(String userId, String alarmId, AlarmRequest request) {
        MedicationAlarm alarm = findOwned(userId, alarmId);
        alarm.setAlarmTime(request.alarmTime().format(TIME_FORMAT));
        medicationAlarmRepository.update(alarmId, alarm);
        return toResponse(alarm);
    }

    public void delete(String userId, String alarmId) {
        findOwned(userId, alarmId);
        medicationAlarmRepository.deleteById(alarmId);
    }

    private MedicationAlarm findOwned(String userId, String alarmId) {
        MedicationAlarm alarm = medicationAlarmRepository.findById(alarmId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_ALARM_NOT_FOUND));

        if (!alarm.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.MEDICATION_ALARM_FORBIDDEN);
        }
        return alarm;
    }

    private AlarmResponse toResponse(MedicationAlarm alarm) {
        return new AlarmResponse(alarm.getId(), LocalTime.parse(alarm.getAlarmTime(), TIME_FORMAT));
    }
}
