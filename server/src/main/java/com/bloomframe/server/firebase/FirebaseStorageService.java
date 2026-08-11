package com.bloomframe.server.firebase;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

public class FirebaseStorageService implements StorageService {

    private final FirebaseAppHolder holder;
    private final String bucketName;

    public FirebaseStorageService(FirebaseAppHolder holder, String bucketName) {
        this.holder = holder;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadMedicinePhoto(String uid, String reminderId, byte[] data, String contentType) {
        String path = "medicine-photos/" + uid + "/" + reminderId + ".jpg";
        Bucket bucket = StorageClient.getInstance(holder.app()).bucket(bucketName);
        Blob blob = bucket.create(path, data, contentType);
        return blob.getName();
    }
}
