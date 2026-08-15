package com.bloomframe.server.ai;

import com.bloomframe.server.config.R2Properties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

public class R2MedicinePhotoStorage implements MedicinePhotoStorage {

    private static final Logger log = LoggerFactory.getLogger(R2MedicinePhotoStorage.class);

    private final S3Client s3;
    private final String bucket;

    public R2MedicinePhotoStorage(R2Properties properties) {
        this.bucket = properties.getBucket();
        this.s3 = S3Client.builder()
                .endpointOverride(URI.create("https://" + properties.getAccountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKeyId(), properties.getSecretAccessKey())))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
        log.info("AI medicine photo storage using R2 bucket={}", bucket);
    }

    @Override
    public String uploadMedicinePhoto(String uid, String reminderId, byte[] data, String contentType) {
        String path = "medicine-photos/" + uid + "/" + reminderId + ".jpg";
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(path)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(data));
        return path;
    }

    @Override
    public byte[] download(String storagePath) {
        return s3.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(storagePath)
                        .build())
                .asByteArray();
    }

    @PreDestroy
    void close() {
        s3.close();
    }
}
