package com.bloomframe.server.ai.dto;

import java.util.List;

public record MedicinePhotoAnalysisResult(
        List<MedicineAnalysisDto> medications,
        String rawText,
        double confidence
) {
    public MedicinePhotoAnalysisResult {
        medications = medications == null ? List.of() : List.copyOf(medications);
    }
}
