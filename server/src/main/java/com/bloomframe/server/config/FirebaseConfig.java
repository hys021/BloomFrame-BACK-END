package com.bloomframe.server.config;

import com.bloomframe.server.firebase.FirebaseAppHolder;
import com.bloomframe.server.firebase.FirestoreHolder;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    FirebaseAppHolder firebaseAppHolder(FirebaseProperties properties, ResourceLoader resourceLoader) {
        if (!properties.isEnabled()) {
            log.warn("Firebase disabled ??set firebase.credentials-path in application-local.yml");
            return FirebaseAppHolder.disabled();
        }

        try (InputStream credentialsStream = openCredentials(properties.getCredentialsPath(), resourceLoader)) {
            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream));

            if (properties.getStorageBucket() != null && !properties.getStorageBucket().isBlank()) {
                builder.setStorageBucket(properties.getStorageBucket());
            }

            FirebaseApp app;
            if (FirebaseApp.getApps().isEmpty()) {
                app = FirebaseApp.initializeApp(builder.build());
            } else {
                app = FirebaseApp.getInstance();
            }

            log.info("Firebase Admin initialized: projectId={}", app.getOptions().getProjectId());
            return FirebaseAppHolder.enabled(app);
        } catch (IOException e) {
            log.error("Firebase init failed ??running without Firebase: {}", e.getMessage());
            return FirebaseAppHolder.disabled();
        }
    }

    @Bean
    FirestoreHolder firestoreHolder(FirebaseAppHolder appHolder) {
        if (!appHolder.enabled()) {
            log.warn("Firestore disabled — Firebase app is not initialized");
            return FirestoreHolder.disabled();
        }
        Firestore firestore = FirestoreClient.getFirestore(appHolder.app());
        return FirestoreHolder.enabled(firestore);
    }

    private InputStream openCredentials(String path, ResourceLoader resourceLoader) throws IOException {
        if (path.startsWith("classpath:") || path.startsWith("file:")) {
            Resource resource = resourceLoader.getResource(path);
            return resource.getInputStream();
        }
        return Files.newInputStream(Path.of(path));
    }
}
