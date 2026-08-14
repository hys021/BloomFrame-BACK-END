package com.bloomframe.server.ai.dto;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MedicationFieldMapper {

    private static final Pattern DAILY_COUNT = Pattern.compile("1\\s*일\\s*(\\d+)\\s*회");
    private static final List<String> MEAL_TIMINGS = List.of("식전", "식후", "취침 전");

    static int dosePerDay(MedicineAnalysisDto analysis, Integer existing) {
        if (analysis != null && analysis.dosePerDay() != null && analysis.dosePerDay() > 0) {
            return analysis.dosePerDay();
        }
        if (analysis != null && analysis.timings() != null && !analysis.timings().isEmpty()) {
            return analysis.timings().size();
        }
        if (analysis != null && analysis.frequency() != null) {
            Matcher matcher = DAILY_COUNT.matcher(analysis.frequency());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        if (existing != null && existing > 0) {
            return existing;
        }
        return 1;
    }

    static String timing(MedicineAnalysisDto analysis, String existing) {
        if (analysis != null) {
            String mapped = normalizeTiming(analysis.timing());
            if (mapped != null) {
                return mapped;
            }
            mapped = timingFromText(analysis.rawText());
            if (mapped != null) {
                return mapped;
            }
            mapped = timingFromText(analysis.frequency());
            if (mapped != null) {
                return mapped;
            }
            mapped = timingFromClocks(analysis.timings());
            if (mapped != null) {
                return mapped;
            }
        }
        String fromExisting = normalizeTiming(existing);
        return fromExisting != null ? fromExisting : "식후";
    }

    private static String normalizeTiming(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        for (String allowed : MEAL_TIMINGS) {
            if (allowed.equals(trimmed)) {
                return allowed;
            }
        }
        if (trimmed.contains("취침")) {
            return "취침 전";
        }
        if (trimmed.contains("식전")) {
            return "식전";
        }
        if (trimmed.contains("식후")) {
            return "식후";
        }
        return null;
    }

    private static String timingFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return normalizeTiming(text);
    }

    private static String timingFromClocks(List<String> timings) {
        if (timings == null || timings.isEmpty()) {
            return null;
        }
        for (String clock : timings) {
            Integer hour = hourOf(clock);
            if (hour != null && hour >= 21) {
                return "취침 전";
            }
        }
        return "식후";
    }

    private static Integer hourOf(String clock) {
        if (clock == null || !clock.contains(":")) {
            return null;
        }
        try {
            return Integer.parseInt(clock.trim().split(":")[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private MedicationFieldMapper() {}
}
