package com.bloomframe.server.healthcondition.controller;

import com.bloomframe.server.common.security.CustomUserPrincipal;
import com.bloomframe.server.healthcondition.dto.request.HealthConditionRequest;
import com.bloomframe.server.healthcondition.dto.response.HealthConditionResponse;
import com.bloomframe.server.healthcondition.service.HealthConditionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-conditions")
public class HealthConditionController {

    private final HealthConditionService healthConditionService;

    public HealthConditionController(HealthConditionService healthConditionService) {
        this.healthConditionService = healthConditionService;
    }

    @GetMapping
    public List<HealthConditionResponse> getConditions(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return healthConditionService.getConditions(principal.getUserId());
    }

    @PostMapping
    public ResponseEntity<HealthConditionResponse> register(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                              @Valid @RequestBody HealthConditionRequest request) {
        HealthConditionResponse response = healthConditionService.register(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{conditionId}")
    public HealthConditionResponse update(@AuthenticationPrincipal CustomUserPrincipal principal,
                                           @PathVariable String conditionId,
                                           @Valid @RequestBody HealthConditionRequest request) {
        return healthConditionService.update(principal.getUserId(), conditionId, request);
    }

    @DeleteMapping("/{conditionId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                                        @PathVariable String conditionId) {
        healthConditionService.delete(principal.getUserId(), conditionId);
        return ResponseEntity.noContent().build();
    }
}
