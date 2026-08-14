package com.bloomframe.server.ai;

public record NewsletterContext(
        String userName,
        String drugName,
        String dosage,
        String frequency,
        String reminderCategory,
        String trigger,
        String kind,
        String timeOfDay
) {
    public static final String KIND_MEDICINE = "medicine";
    public static final String KIND_HEALTH = "health";
}
