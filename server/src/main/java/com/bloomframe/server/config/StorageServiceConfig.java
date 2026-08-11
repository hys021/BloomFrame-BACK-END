package com.bloomframe.server.config;

import com.bloomframe.server.firebase.FirebaseAppHolder;
import com.bloomframe.server.firebase.FirebaseStorageService;
import com.bloomframe.server.firebase.NoopStorageService;
import com.bloomframe.server.firebase.StorageService;
import com.bloomframe.server.storage.R2StorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class StorageServiceConfig {

    @Bean
    StorageService storageService(
            R2Properties r2,
            FirebaseAppHolder holder,
            FirebaseProperties firebase) {
        if (r2.isEnabled()) {
            return new R2StorageService(r2);
        }
        if (holder.enabled()
                && firebase.getStorageBucket() != null
                && !firebase.getStorageBucket().isBlank()) {
            return new FirebaseStorageService(holder, firebase.getStorageBucket());
        }
        return new NoopStorageService();
    }
}
