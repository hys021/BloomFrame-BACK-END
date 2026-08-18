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
    void rejectsEmpty() {
        assertThrows(IllegalStateException.class, () -> AiJsonMapper.extractJsonObject("   "));
    }
}
