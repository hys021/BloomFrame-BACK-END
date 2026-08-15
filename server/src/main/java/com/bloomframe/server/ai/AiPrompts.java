package com.bloomframe.server.ai;

final class AiPrompts {

    static final String MEDICINE = """
            Extract medication info from this photo of a pill box, blister pack, or Korean prescription label.
            Reply with JSON only:
            {"drugName":"","dosage":"","frequency":"","dosePerDay":3,"timing":"식후","timings":["08:00"],"rawText":"","confidence":0.0}
            - drugName: brand or ingredient as printed
            - dosage: per-dose amount (e.g. 1정, 500mg)
            - frequency: how often (e.g. 1일 3회)
            - dosePerDay: integer times per day
            - timing: exactly one of 식전, 식후, 취침 전
            - timings: 24h clock times. Map 아침/점심/저녁/취침 to 08:00/12:00/18:00/21:00 when clocks are missing
            - rawText: key text you read
            - confidence: 0 to 1
            If unreadable, keep strings empty and lower confidence. Korean labels are expected.
            """;

    static String newsletter(NewsletterContext context) {
        if (NewsletterContext.KIND_HEALTH.equals(context.kind())) {
            return """
                    Write a short Korean everyday health tip newsletter for an older adult or caregiver.
                    JSON only:
                    {"title":"","body":"","tips":["",""]}
                    Context:
                    - name: %s
                    - timeOfDay: %s
                    - category: %s
                    This is NOT about a specific medicine. Do not mention side effects of a named drug.
                    Pick one practical topic that fits the time of day
                    (morning: water, light stretch, breakfast; afternoon: walk, posture, salt;
                    evening: sleep, screens, tonight's water).
                    Vary the topic so it does not repeat the same advice every time.
                    Easy Korean, 2-4 short sentences. Title under 24 characters (e.g. 오늘 알아두면 좋은 팁).
                    No congratulations, no "수고하셨어요".
                    """.formatted(
                    blankTo(context.userName(), "사용자"),
                    blankTo(context.timeOfDay(), "afternoon"),
                    blankTo(context.reminderCategory(), "other"));
        }
        return """
                Write a short Korean newsletter about common precautions after taking this medicine.
                JSON only:
                {"title":"","body":"","tips":["",""]}
                Context:
                - name: %s
                - drug: %s
                - dosage: %s
                - frequency: %s
                Body: 2-4 simple sentences on common side effects or cautions
                (drowsiness, stomach upset, dizziness, food/alcohol, rest).
                Tips: 2-3 concrete points. Easy Korean, no scare tactics.
                This is general information, not a diagnosis or a dose change.
                Do not invent rare or alarming side effects.
                End body: unusual symptoms → ask a pharmacist or doctor.
                Title under 24 characters (e.g. 복용 후 주의점). No "수고하셨어요".
                """.formatted(
                blankTo(context.userName(), "사용자"),
                blankTo(context.drugName(), "약"),
                blankTo(context.dosage(), ""),
                blankTo(context.frequency(), ""));
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private AiPrompts() {}
}
