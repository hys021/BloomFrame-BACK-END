package com.bloomframe.server.controller;

import com.bloomframe.server.ai.AiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiHealthController {

    private final AiClient aiClient;

    public AiHealthController(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("ai", aiClient.provider());
    }
}
