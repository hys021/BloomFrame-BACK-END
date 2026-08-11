package com.bloomframe.server.firebase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class NoopStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(NoopStorageService.class);

    @Override
    public String uploadMedicinePhoto(String uid, String reminderId, byte[] data, String contentType) {
        String path = "medicine-photos/" + uid + "/" + reminderId + ".jpg";
        log.info("[noop] uploadMedicinePhoto path={} bytes={}", path, data.length);
        return path;
    }
}
