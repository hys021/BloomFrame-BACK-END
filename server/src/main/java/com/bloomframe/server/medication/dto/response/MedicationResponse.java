package com.bloomframe.server.medication.dto.response;

import com.bloomframe.server.medication.model.Medication;

public record MedicationResponse(
        String id,
        String name,
        Integer dosePerDay,
        String timing,
        String imageUrl
) {
    public static MedicationResponse from(Medication m) {
        return new MedicationResponse(m.getId(), m.getName(), m.getDosePerDay(), m.getTiming(), m.getImageUrl());
    }
}
