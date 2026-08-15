package com.bloomframe.server.ai.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MedicationFieldMapperTest {

    @Test
    void dosePerDayFromFrequency() {
        MedicineAnalysisDto analysis = new MedicineAnalysisDto(
                "타이레놀", "1정", "1일 3회", null, "", List.of(), "", 1.0);
        assertEquals(3, MedicationFieldMapper.dosePerDay(analysis, 1));
    }

    @Test
    void timing취침FromClock() {
        MedicineAnalysisDto analysis = new MedicineAnalysisDto(
                "약", "", "", null, "", List.of("21:00"), "", 1.0);
        assertEquals("취침 전", MedicationFieldMapper.timing(analysis, null));
    }

    @Test
    void timing식후Default() {
        MedicineAnalysisDto analysis = new MedicineAnalysisDto(
                "약", "", "", null, "", List.of("08:00", "12:00"), "", 1.0);
        assertEquals("식후", MedicationFieldMapper.timing(analysis, null));
    }
}
