package com.bloomframe.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ReminderDto(
        String id,
        @NotBlank @Pattern(regexp = "medicine|exercise|other") String category,
        @NotEmpty List<String> times,
        @NotBlank String imageKey,
        String photoStoragePath,
        MedicineAnalysisDto analysis,
        boolean confirmed
) {
    public ReminderDto withId(String newId) {
        return new ReminderDto(newId, category, times, imageKey, photoStoragePath, analysis, confirmed);
    }
}
