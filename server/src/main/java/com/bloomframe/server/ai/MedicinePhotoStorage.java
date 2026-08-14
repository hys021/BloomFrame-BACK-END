package com.bloomframe.server.ai;

public interface MedicinePhotoStorage {

    String uploadMedicinePhoto(String uid, String reminderId, byte[] data, String contentType);

    byte[] download(String storagePath);
}
