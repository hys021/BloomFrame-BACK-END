package com.bloomframe.server.medication.service;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.medication.dto.request.MedicationRequest;
import com.bloomframe.server.medication.dto.response.MedicationResponse;
import com.bloomframe.server.medication.model.Medication;
import com.bloomframe.server.medication.repository.MedicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<MedicationResponse> getMedications(String userId) {
        return medicationRepository.findAllByUserId(userId).stream()
                .map(MedicationResponse::from)
                .toList();
    }

    public MedicationResponse register(String userId, MedicationRequest request) {
        Medication medication = new Medication(
                userId,
                request.name(),
                request.dosePerDay(),
                request.timing(),
                request.imageUrl()
        );
        medicationRepository.save(medication);
        return MedicationResponse.from(medication);
    }

    public MedicationResponse update(String userId, String medicationId, MedicationRequest request) {
        Medication medication = findOwned(userId, medicationId);

        medication.setName(request.name());
        medication.setDosePerDay(request.dosePerDay());
        medication.setTiming(request.timing());
        medication.setImageUrl(request.imageUrl());

        medicationRepository.update(medicationId, medication);
        return MedicationResponse.from(medication);
    }

    public void delete(String userId, String medicationId) {
        findOwned(userId, medicationId); // 소유권 검증 먼저
        medicationRepository.deleteById(medicationId);
    }

    private Medication findOwned(String userId, String medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND));

        if (!medication.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.MEDICATION_FORBIDDEN);
        }
        return medication;
    }
}
