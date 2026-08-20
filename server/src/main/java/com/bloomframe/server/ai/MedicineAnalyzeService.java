package com.bloomframe.server.ai;

import com.bloomframe.server.ai.dto.AiMedicationDto;
import com.bloomframe.server.ai.dto.MedicineAnalysisDto;
import com.bloomframe.server.ai.dto.MedicineAnalyzeResponse;
import com.bloomframe.server.ai.dto.MedicinePhotoAnalysisResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MedicineAnalyzeService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");
    private static final int MAX_BYTES = 5 * 1024 * 1024;

    private final AiClient aiClient;
    private final AiDataStore aiDataStore;
    private final MedicinePhotoStorage photoStorage;

    public MedicineAnalyzeService(
            AiClient aiClient,
            AiDataStore aiDataStore,
            MedicinePhotoStorage photoStorage) {
        this.aiClient = aiClient;
        this.aiDataStore = aiDataStore;
        this.photoStorage = photoStorage;
    }

    public MedicineAnalyzeResponse analyze(String uid, String medicationId, MultipartFile file) {
        AiMedicationDto medication = aiDataStore.getMedication(uid, medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found: " + medicationId));

        byte[] image;
        String contentType;
        String imageUrl = medication.imageUrl();

        if (file != null && !file.isEmpty()) {
            contentType = normalizeContentType(file.getContentType());
            image = readBytes(file);
            if (image.length > MAX_BYTES) {
                throw new IllegalArgumentException("Photo must be 5MB or smaller");
            }
            if (!ALLOWED_TYPES.contains(contentType)) {
                throw new IllegalArgumentException("Photo must be jpeg, png, webp, or gif");
            }
            imageUrl = photoStorage.uploadMedicinePhoto(uid, medicationId, image, contentType);
        } else {
            String storagePath = r2Key(imageUrl, uid, medicationId);
            image = photoStorage.download(storagePath);
            contentType = "image/jpeg";
            imageUrl = storagePath;
        }

        List<String> healthConditions = aiDataStore.listHealthConditionNames(uid);
        MedicinePhotoAnalysisResult photoAnalysis =
                aiClient.analyzeMedicinePhoto(image, contentType, healthConditions);
        if (photoAnalysis.medications().isEmpty()) {
            throw new IllegalArgumentException("Could not read any medicine from the photo");
        }

        List<AiMedicationDto> saved = new ArrayList<>();
        boolean first = true;
        for (MedicineAnalysisDto analysis : photoAnalysis.medications()) {
            if (first) {
                AiMedicationDto updated = medication.withPhotoAndAnalysis(imageUrl, analysis);
                saved.add(aiDataStore.updateMedication(uid, medicationId, updated));
                first = false;
            } else {
                AiMedicationDto draft = new AiMedicationDto(
                        null,
                        medication.name(),
                        medication.dosePerDay(),
                        medication.timing(),
                        imageUrl,
                        null);
                AiMedicationDto created = draft.withPhotoAndAnalysis(imageUrl, analysis);
                saved.add(aiDataStore.createMedication(uid, created));
            }
        }
        return new MedicineAnalyzeResponse(saved);
    }

    private static String r2Key(String imageUrl, String uid, String medicationId) {
        if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        if (imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("Send the photo file; HTTP imageUrl cannot be downloaded from R2");
        }
        return "medicine-photos/" + uid + "/" + medicationId + ".jpg";
    }

    private static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read uploaded photo");
        }
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "image/jpeg";
        }
        String mime = contentType.toLowerCase(Locale.ROOT);
        int semi = mime.indexOf(';');
        if (semi > 0) {
            mime = mime.substring(0, semi).trim();
        }
        if ("image/jpg".equals(mime)) {
            return "image/jpeg";
        }
        return mime;
    }
}
