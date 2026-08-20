package com.bloomframe.server.ai.dto;

import java.util.List;

public record MedicineAnalyzeResponse(
        List<AiMedicationDto> medications
) {
    public MedicineAnalyzeResponse {
        medications = medications == null ? List.of() : List.copyOf(medications);
    }
}
