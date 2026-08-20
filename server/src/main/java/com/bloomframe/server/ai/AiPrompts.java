package com.bloomframe.server.ai;

import java.util.List;
import java.util.stream.Collectors;

final class AiPrompts {

    static String medicine(List<String> healthConditions) {
        String conditions = formatHealthConditions(healthConditions);
        return """
                Extract medication info from this photo of a Korean pharmacy pill bag, pill box, blister pack, or prescription label.
                List EVERY separate drug visible on the bag or label — one entry per drug, not one entry for the whole bag.
                Reply with JSON only:
                {"medications":[{"drugName":"","dosage":"","frequency":"","dosePerDay":3,"timing":"식후","timings":["08:00"],"rawText":"","confidence":0.0}],"rawText":"","confidence":0.0}
                Per medication object:
                - drugName: brand or ingredient as printed
                - dosage: per-dose amount (e.g. 1정, 500mg)
                - frequency: how often (e.g. 1일 3회)
                - dosePerDay: integer times per day
                - timing: exactly one of 식전, 식후, 취침 전
                - timings: 24h clock times. Map 아침/점심/저녁/취침 to 08:00/12:00/18:00/21:00 when clocks are missing
                - rawText: key text you read for that drug
                - confidence: 0 to 1 for that drug
                Top-level rawText: overall text from the bag. Top-level confidence: overall read quality 0 to 1.
                If unreadable, return medications:[] and lower confidence. Korean labels are expected.
                %s
                """.formatted(conditions);
    }

    static String newsletter(NewsletterContext context) {
        String conditions = formatHealthConditions(context.healthConditions());
        if (NewsletterContext.KIND_HEALTH.equals(context.kind())) {
            return """
                    Write a short Korean everyday health tip newsletter for an older adult or caregiver.
                    JSON only:
                    {"title":"","body":"","tips":["",""]}
                    Context:
                    - name: %s
                    - timeOfDay: %s
                    - category: %s
                    %s
                    This is NOT about a specific medicine. Do not mention side effects of a named drug unless relevant to listed conditions.
                    Pick one practical topic that fits the time of day and the user's health conditions when provided
                    (morning: water, light stretch, breakfast; afternoon: walk, posture, salt;
                    evening: sleep, screens, tonight's water).
                    When health conditions are listed, tailor the tip to them in plain language (no diagnosis).
                    Vary the topic so it does not repeat the same advice every time.
                    Easy Korean, 2-4 short sentences. Title under 24 characters (e.g. 오늘 알아두면 좋은 팁).
                    No congratulations, no "수고하셨어요".
                    """.formatted(
                    blankTo(context.userName(), "사용자"),
                    blankTo(context.timeOfDay(), "afternoon"),
                    blankTo(context.reminderCategory(), "other"),
                    conditions);
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
                %s
                Body: 2-4 simple sentences on common side effects or cautions
                (drowsiness, stomach upset, dizziness, food/alcohol, rest).
                When health conditions are listed, add one sentence on what to watch for with this drug (general info only).
                Tips: 2-3 concrete points. Easy Korean, no scare tactics.
                This is general information, not a diagnosis or a dose change.
                Do not invent rare or alarming side effects.
                End body: unusual symptoms → ask a pharmacist or doctor.
                Title under 24 characters (e.g. 복용 후 주의점). No "수고하셨어요".
                """.formatted(
                blankTo(context.userName(), "사용자"),
                blankTo(context.drugName(), "약"),
                blankTo(context.dosage(), ""),
                blankTo(context.frequency(), ""),
                conditions);
    }

    private static String formatHealthConditions(List<String> healthConditions) {
        if (healthConditions == null || healthConditions.isEmpty()) {
            return "- healthConditions: (none registered)";
        }
        String joined = healthConditions.stream()
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.joining(", "));
        if (joined.isBlank()) {
            return "- healthConditions: (none registered)";
        }
        return "- healthConditions: " + joined;
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private AiPrompts() {}
}
