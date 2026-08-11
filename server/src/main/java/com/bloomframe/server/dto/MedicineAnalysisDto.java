package com.bloomframe.server.dto;

import java.util.List;

public record MedicineAnalysisDto(
        String drugName,
        String dosage,
        String frequency,
        List<String> timings,
        String rawText,
        double confidence
) {}
