package com.bloomframe.server.ai.dto;

public record AiMedicationDto(
        String id,
        String name,
        Integer dosePerDay,
        String timing,
        String imageUrl,
        MedicineAnalysisDto analysis
) {
    public AiMedicationDto withPhotoAndAnalysis(String newImageUrl, MedicineAnalysisDto newAnalysis) {
        String nextName = name;
        if (newAnalysis != null
                && newAnalysis.drugName() != null
                && !newAnalysis.drugName().isBlank()) {
            nextName = newAnalysis.drugName();
        }
        return new AiMedicationDto(
                id,
                nextName,
                MedicationFieldMapper.dosePerDay(newAnalysis, dosePerDay),
                MedicationFieldMapper.timing(newAnalysis, timing),
                newImageUrl,
                newAnalysis);
    }
}
