package com.bloomframe.server.config;

import com.bloomframe.server.ai.AiClient;
import com.bloomframe.server.ai.AiDataStore;
import com.bloomframe.server.ai.FirestoreAiDataStore;
import com.bloomframe.server.ai.GeminiAiClient;
import com.bloomframe.server.ai.MedicinePhotoStorage;
import com.bloomframe.server.ai.NoopAiClient;
import com.bloomframe.server.ai.NoopAiDataStore;
import com.bloomframe.server.ai.NoopMedicinePhotoStorage;
import com.bloomframe.server.ai.R2MedicinePhotoStorage;
import com.bloomframe.server.firebase.FcmService;
import com.bloomframe.server.firebase.FirebaseAppHolder;
import com.bloomframe.server.firebase.FirebaseFcmService;
import com.bloomframe.server.firebase.FirestoreHolder;
import com.bloomframe.server.firebase.NoopFcmService;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AiProperties.class)
public class AiServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(AiServiceConfig.class);

    @Bean
    AiClient aiClient(AiProperties properties, ObjectMapper objectMapper) {
        if (properties.getGemini().isConfigured()) {
            return new GeminiAiClient(properties, objectMapper);
        }
        log.warn("Gemini API key missing — analyze/newsletter use mock data. Set ai.gemini.api-key or GEMINI_API_KEY");
        return new NoopAiClient();
    }

    @Bean
    AiDataStore aiDataStore(FirestoreHolder holder) {
        if (holder.enabled()) {
            return new FirestoreAiDataStore(holder);
        }
        log.warn("Firestore disabled — AI analyze/newsletter use in-memory store");
        return new NoopAiDataStore();
    }

    @Bean
    FcmService fcmService(FirebaseAppHolder holder) {
        if (holder.enabled()) {
            return new FirebaseFcmService(holder);
        }
        return new NoopFcmService();
    }

    @Bean
    MedicinePhotoStorage medicinePhotoStorage(R2Properties r2) {
        if (r2.isEnabled()) {
            return new R2MedicinePhotoStorage(r2);
        }
        return new NoopMedicinePhotoStorage();
    }
}
