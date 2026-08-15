package com.bloomframe.server.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoopMedicinePhotoStorage implements MedicinePhotoStorage {

    private static final Logger log = LoggerFactory.getLogger(NoopMedicinePhotoStorage.class);
    private final Map<String, byte[]> photos = new ConcurrentHashMap<>();

    @Override
    public String uploadMedicinePhoto(String uid, String reminderId, byte[] data, String contentType) {
        String path = "medicine-photos/" + uid + "/" + reminderId + ".jpg";
        photos.put(path, data);
        log.info("[noop] uploadMedicinePhoto path={} bytes={}", path, data.length);
        return path;
    }

    @Override
    public byte[] download(String storagePath) {
        byte[] data = photos.get(storagePath);
        if (data == null) {
            throw new IllegalArgumentException("Photo not found: " + storagePath);
        }
        return data;
    }
}
