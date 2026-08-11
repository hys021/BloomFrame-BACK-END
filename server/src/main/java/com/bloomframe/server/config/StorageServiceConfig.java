package com.bloomframe.server.config;

import com.bloomframe.server.storage.NoopStorageService;
import com.bloomframe.server.storage.R2StorageService;
import com.bloomframe.server.storage.StorageService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class StorageServiceConfig {

    @Bean
    StorageService storageService(R2Properties r2) {
        if (r2.isEnabled()) {
            return new R2StorageService(r2);
        }
        return new NoopStorageService();
    }
}
