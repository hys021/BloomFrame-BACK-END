package com.bloomframe.server.alarm.controller;

import com.bloomframe.server.alarm.dto.request.ExerciseAlarmRequest;
import com.bloomframe.server.alarm.dto.response.ExerciseAlarmResponse;
import com.bloomframe.server.alarm.service.ExerciseAlarmService;
import com.bloomframe.server.common.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercise-alarms")
public class ExerciseAlarmController {

    private final ExerciseAlarmService exerciseAlarmService;

    public ExerciseAlarmController(ExerciseAlarmService exerciseAlarmService) {
        this.exerciseAlarmService = exerciseAlarmService;
    }

    @GetMapping
    public List<ExerciseAlarmResponse> getAlarms(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return exerciseAlarmService.getAlarms(principal.getUserId());
    }

    @PostMapping
    public ResponseEntity<ExerciseAlarmResponse> register(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                            @Valid @RequestBody ExerciseAlarmRequest request) {
        ExerciseAlarmResponse response = exerciseAlarmService.register(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{alarmId}")
    public ExerciseAlarmResponse update(@AuthenticationPrincipal CustomUserPrincipal principal,
                                         @PathVariable String alarmId,
                                         @Valid @RequestBody ExerciseAlarmRequest request) {
        return exerciseAlarmService.update(principal.getUserId(), alarmId, request);
    }

    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                                        @PathVariable String alarmId) {
        exerciseAlarmService.delete(principal.getUserId(), alarmId);
        return ResponseEntity.noContent().build();
    }
}
