package com.bloomframe.server.firebase;

public interface StorageService {

    String uploadMedicinePhoto(String uid, String reminderId, byte[] data, String contentType);
}
