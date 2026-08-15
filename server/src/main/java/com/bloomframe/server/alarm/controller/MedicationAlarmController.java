package com.bloomframe.server.alarm.controller;

import com.bloomframe.server.alarm.dto.request.AlarmRequest;
import com.bloomframe.server.alarm.dto.response.AlarmResponse;
import com.bloomframe.server.alarm.service.MedicationAlarmService;
import com.bloomframe.server.common.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medication-alarms")
public class MedicationAlarmController {

    private final MedicationAlarmService medicationAlarmService;

    public MedicationAlarmController(MedicationAlarmService medicationAlarmService) {
        this.medicationAlarmService = medicationAlarmService;
    }

    @GetMapping
    public List<AlarmResponse> getAlarms(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return medicationAlarmService.getAlarms(principal.getUserId());
    }

    @PostMapping
    public ResponseEntity<AlarmResponse> register(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                   @Valid @RequestBody AlarmRequest request) {
        AlarmResponse response = medicationAlarmService.register(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{alarmId}")
    public AlarmResponse update(@AuthenticationPrincipal CustomUserPrincipal principal,
                                 @PathVariable String alarmId,
                                 @Valid @RequestBody AlarmRequest request) {
        return medicationAlarmService.update(principal.getUserId(), alarmId, request);
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                                        @PathVariable String alarmId) {
        medicationAlarmService.delete(principal.getUserId(), alarmId);
        return ResponseEntity.noContent().build();
    }
}
