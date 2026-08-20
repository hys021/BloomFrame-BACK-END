package com.bloomframe.server.ai;

import java.util.List;

public record NewsletterContext(
        String userName,
        String drugName,
        String dosage,
        String frequency,
        String reminderCategory,
        String trigger,
        String kind,
        String timeOfDay,
        List<String> healthConditions
) {
    public NewsletterContext {
        healthConditions = healthConditions == null ? List.of() : List.copyOf(healthConditions);
    }

    public static final String KIND_MEDICINE = "medicine";
    public static final String KIND_HEALTH = "health";
}
