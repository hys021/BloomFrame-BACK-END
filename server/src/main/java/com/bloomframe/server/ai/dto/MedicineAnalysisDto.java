package com.bloomframe.server.ai.dto;

import java.util.List;

public record MedicineAnalysisDto(
        String drugName,
        String dosage,
        String frequency,
        Integer dosePerDay,
        String timing,
        List<String> timings,
        String rawText,
        double confidence
) {}
