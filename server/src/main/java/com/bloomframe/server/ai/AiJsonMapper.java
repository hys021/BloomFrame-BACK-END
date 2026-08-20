package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.MedicineAnalysisDto;
import com.bloomframe.server.ai.dto.MedicinePhotoAnalysisResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

final class AiJsonMapper {

    private final ObjectMapper objectMapper;

    AiJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    MedicinePhotoAnalysisResult toPhotoAnalysis(String raw) {
        JsonNode node = parseObject(raw);
        List<MedicineAnalysisDto> medications = new ArrayList<>();
        JsonNode medicationsNode = node.get("medications");
        if (medicationsNode != null && medicationsNode.isArray()) {
            medicationsNode.forEach(item -> medications.add(toAnalysisFromNode(item)));
        } else if (node.has("drugName")) {
            medications.add(toAnalysisFromNode(node));
        }
        double confidence = node.path("confidence").asDouble(0.0);
        if (confidence == 0.0 && !medications.isEmpty()) {
            confidence = medications.stream()
                    .mapToDouble(MedicineAnalysisDto::confidence)
                    .average()
                    .orElse(0.0);
        }
        return new MedicinePhotoAnalysisResult(
                List.copyOf(medications),
                text(node, "rawText"),
                confidence);
    }

    MedicineAnalysisDto toAnalysis(String raw) {
        return toAnalysisFromNode(parseObject(raw));
    }

    private MedicineAnalysisDto toAnalysisFromNode(JsonNode node) {
        List<String> timings = new ArrayList<>();
        JsonNode timingsNode = node.get("timings");
        if (timingsNode != null && timingsNode.isArray()) {
            timingsNode.forEach(item -> timings.add(item.asText()));
        }
        return new MedicineAnalysisDto(
                text(node, "drugName"),
                text(node, "dosage"),
                text(node, "frequency"),
                node.has("dosePerDay") && node.get("dosePerDay").isNumber()
                        ? node.get("dosePerDay").asInt()
                        : null,
                text(node, "timing"),
                List.copyOf(timings),
                text(node, "rawText"),
                node.path("confidence").asDouble(0.0)
        );
    }

    NewsletterContent toNewsletter(String raw) {
        JsonNode node = parseObject(raw);
        List<String> tips = new ArrayList<>();
        JsonNode tipsNode = node.get("tips");
        if (tipsNode != null && tipsNode.isArray()) {
            tipsNode.forEach(item -> tips.add(item.asText()));
        }
        return new NewsletterContent(
                text(node, "title"),
                text(node, "body"),
                List.copyOf(tips)
        );
    }

    JsonNode parseObject(String raw) {
        try {
            return objectMapper.readTree(extractJsonObject(raw));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("AI response was not valid JSON", e);
        }
    }

    static String extractJsonObject(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("AI returned an empty response");
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int fence = text.lastIndexOf("```");
            if (firstNewline > 0 && fence > firstNewline) {
                text = text.substring(firstNewline + 1, fence).trim();
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("AI response did not contain a JSON object");
        }
        return text.substring(start, end + 1);
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }
}
