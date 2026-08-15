package com.bloomframe.server.medication.controller;

import com.bloomframe.server.common.security.CustomUserPrincipal;
import com.bloomframe.server.medication.dto.request.MedicationRequest;
import com.bloomframe.server.medication.dto.response.MedicationResponse;
import com.bloomframe.server.medication.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<MedicationResponse> getMedications(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return medicationService.getMedications(principal.getUserId());
    }

    @PostMapping
    public ResponseEntity<MedicationResponse> register(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                         @Valid @RequestBody MedicationRequest request) {
        MedicationResponse response = medicationService.register(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{medicationId}")
    public MedicationResponse update(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable String medicationId,
                                      @Valid @RequestBody MedicationRequest request) {
        return medicationService.update(principal.getUserId(), medicationId, request);
    }

    @DeleteMapping("/{medicationId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                                        @PathVariable String medicationId) {
        medicationService.delete(principal.getUserId(), medicationId);
        return ResponseEntity.noContent().build();
    }
}
