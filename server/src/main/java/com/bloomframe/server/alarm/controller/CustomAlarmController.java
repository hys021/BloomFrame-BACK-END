package com.bloomframe.server.alarm.controller;

import com.bloomframe.server.alarm.dto.request.CustomAlarmRequest;
import com.bloomframe.server.alarm.dto.response.CustomAlarmResponse;
import com.bloomframe.server.alarm.service.CustomAlarmService;
import com.bloomframe.server.common.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/custom-alarms")
public class CustomAlarmController {

    private final CustomAlarmService customAlarmService;

    public CustomAlarmController(CustomAlarmService customAlarmService) {
        this.customAlarmService = customAlarmService;
    }

    @GetMapping
    public List<CustomAlarmResponse> getAlarms(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return customAlarmService.getAlarms(principal.getUserId());
    }

    @PostMapping
    public ResponseEntity<CustomAlarmResponse> register(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                          @Valid @RequestBody CustomAlarmRequest request) {
        CustomAlarmResponse response = customAlarmService.register(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{alarmId}")
    public CustomAlarmResponse update(@AuthenticationPrincipal CustomUserPrincipal principal,
                                       @PathVariable String alarmId,
                                       @Valid @RequestBody CustomAlarmRequest request) {
        return customAlarmService.update(principal.getUserId(), alarmId, request);
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                                        @PathVariable String alarmId) {
        customAlarmService.delete(principal.getUserId(), alarmId);
        return ResponseEntity.noContent().build();
    }
}
