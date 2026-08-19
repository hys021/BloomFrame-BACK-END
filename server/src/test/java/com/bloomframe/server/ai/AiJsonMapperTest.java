package com.bloomframe.server.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiJsonMapperTest {

    @Test
    void extractsObjectFromMarkdownFence() {
        String raw = """
                ```json
                {"drugName":"타이레놀"}
                ```
                """;
        assertEquals("{\"drugName\":\"타이레놀\"}", AiJsonMapper.extractJsonObject(raw));
    }

    @Test
    void parsesMultipleMedications() {
        AiJsonMapper mapper = new AiJsonMapper(new tools.jackson.databind.ObjectMapper());
        String raw = """
                {"medications":[
                  {"drugName":"타이레놀","dosage":"1정","frequency":"1일 3회","dosePerDay":3,"timing":"식후","timings":["08:00"],"rawText":"a","confidence":0.9},
                  {"drugName":"오메프라졸","dosage":"1캡슐","frequency":"1일 1회","dosePerDay":1,"timing":"식전","timings":["08:00"],"rawText":"b","confidence":0.8}
                ],"rawText":"bag","confidence":0.85}
                """;
        var result = mapper.toPhotoAnalysis(raw);
        assertEquals(2, result.medications().size());
        assertEquals("타이레놀", result.medications().get(0).drugName());
        assertEquals("오메프라졸", result.medications().get(1).drugName());
    }

    @Test
    void rejectsEmpty() {
        assertThrows(IllegalStateException.class, () -> AiJsonMapper.extractJsonObject("   "));
    }
}
