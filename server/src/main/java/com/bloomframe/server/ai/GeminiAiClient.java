package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.MedicineAnalysisDto;
import com.bloomframe.server.ai.dto.MedicinePhotoAnalysisResult;
import com.bloomframe.server.config.AiProperties;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class GeminiAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;
    private final AiJsonMapper jsonMapper;

    public GeminiAiClient(AiProperties properties, ObjectMapper objectMapper) {
        this.model = properties.getGemini().getModel();
        this.objectMapper = objectMapper;
        this.jsonMapper = new AiJsonMapper(objectMapper);
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(45));
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(factory)
                .defaultHeader("x-goog-api-key", properties.getGemini().getApiKey())
                .build();
        log.info("Gemini AI client initialized: model={}", model);
    }

    @Override
    public String provider() {
        return "gemini";
    }

    @Override
    public MedicinePhotoAnalysisResult analyzeMedicinePhoto(
            byte[] image, String contentType, List<String> healthConditions) {
        ObjectNode body = textAndImageBody(AiPrompts.medicine(healthConditions), image, contentType, 0.1);
        try {
            return jsonMapper.toPhotoAnalysis(generate(body));
        } catch (IllegalStateException e) {
            throw new AiException("Could not read medicine info from the photo", e);
        }
    }

    @Override
    public NewsletterContent generateNewsletter(NewsletterContext context) {
        ObjectNode body = textBody(AiPrompts.newsletter(context), 0.7);
        try {
            return jsonMapper.toNewsletter(generate(body));
        } catch (IllegalStateException e) {
            throw new AiException("Could not generate the newsletter", e);
        }
    }

    private ObjectNode textBody(String prompt, double temperature) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode parts = root.putArray("contents").addObject().putArray("parts");
        parts.addObject().put("text", prompt);
        putGenerationConfig(root, temperature);
        return root;
    }

    private ObjectNode textAndImageBody(String prompt, byte[] image, String contentType, double temperature) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode parts = root.putArray("contents").addObject().putArray("parts");
        parts.addObject().put("text", prompt);
        ObjectNode inline = parts.addObject().putObject("inline_data");
        inline.put("mime_type", mimeType(contentType));
        inline.put("data", Base64.getEncoder().encodeToString(image));
        putGenerationConfig(root, temperature);
        return root;
    }

    private static void putGenerationConfig(ObjectNode root, double temperature) {
        ObjectNode config = root.putObject("generationConfig");
        config.put("temperature", temperature);
        config.put("responseMimeType", "application/json");
    }

    private String generate(ObjectNode body) {
        JsonNode response = postWithRetry(body);
        JsonNode text = response.at("/candidates/0/content/parts/0/text");
        if (text.isMissingNode() || text.asText().isBlank()) {
            String block = response.at("/promptFeedback/blockReason").asText("");
            log.warn("Gemini empty text blockReason={}", block);
            throw new AiException("AI returned an empty result");
        }
        return text.asText();
    }

    private JsonNode postWithRetry(ObjectNode body) {
        try {
            return post(body);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 429) {
                log.warn("Gemini rate-limited, retrying once");
                sleepQuietly(1500);
                try {
                    return post(body);
                } catch (RestClientResponseException retry) {
                    log.warn("Gemini retry failed status={}", retry.getStatusCode().value());
                    throw new AiException("AI is busy, try again in a moment", retry);
                }
            }
            log.warn("Gemini request failed status={} body={}", e.getStatusCode().value(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new AiException("AI model is unavailable. Check ai.gemini.model", e);
            }
            throw new AiException("AI request failed", e);
        } catch (ResourceAccessException e) {
            throw new AiException("AI request timed out", e);
        }
    }

    private JsonNode post(ObjectNode body) {
        return restClient.post()
                .uri("/models/{model}:generateContent", model)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private static String mimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "image/jpeg";
        }
        String mime = contentType.toLowerCase(Locale.ROOT);
        int semi = mime.indexOf(';');
        if (semi > 0) {
            mime = mime.substring(0, semi).trim();
        }
        return mime;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AiException("Interrupted waiting to retry AI", e);
        }
    }
}
