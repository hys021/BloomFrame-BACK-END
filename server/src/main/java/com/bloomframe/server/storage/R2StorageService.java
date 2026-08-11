package com.bloomframe.server.storage;

import com.bloomframe.server.config.R2Properties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

public class R2StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(R2StorageService.class);

    private final S3Client s3;

    public R2StorageService(R2Properties properties) {
        this.s3 = S3Client.builder()
                .endpointOverride(URI.create("https://" + properties.getAccountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey())))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        log.info("R2 storage initialized: bucket={}", properties.getBucket());
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @PreDestroy
    void close() {
        s3.close();
    }
}
