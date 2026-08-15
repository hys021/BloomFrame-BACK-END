package com.bloomframe.server.device.repository;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.device.model.Device;
import com.bloomframe.server.device.model.DeviceStatus;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * "devices" 컬렉션. 문서 ID로 deviceUuid를 그대로 사용합니다.
 * -> 이미 등록된 UUID로 다시 등록 시도하면 존재 여부 체크만으로 바로 막을 수 있어,
 *    SQL의 UNIQUE(device_uuid) 제약과 동일한 효과를 냅니다.
 */
@Repository
public class DeviceRepository {

    private static final String COLLECTION = "devices";

    private final Firestore firestore;

    public DeviceRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference collection() {
        return firestore.collection(COLLECTION);
    }

    public boolean existsByDeviceUuid(String deviceUuid) {
        try {
            return collection().document(deviceUuid).get().get().exists();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Device save(Device device) {
        try {
            collection().document(device.getDeviceUuid()).set(device).get();
            return device;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public Optional<Device> findByDeviceUuid(String deviceUuid) {
        try {
            DocumentSnapshot doc = collection().document(deviceUuid).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(toDevice(doc));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public List<Device> findAllByUserId(String userId) {
        try {
            QuerySnapshot snapshot = collection().whereEqualTo("userId", userId).get().get();
            return snapshot.getDocuments().stream().map(this::toDevice).toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    /** 같은 사용자의 CONNECTED 상태 기기 조회 (동시 연결 1개 제한 로직에서 사용). */
    public Optional<Device> findConnectedByUserId(String userId) {
        try {
            QuerySnapshot snapshot = collection()
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", DeviceStatus.CONNECTED.name())
                    .limit(1)
                    .get().get();
            if (snapshot.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(toDevice(snapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void updateStatus(String deviceUuid, DeviceStatus status) {
        try {
            collection().document(deviceUuid).update("status", status.name()).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void updateName(String deviceUuid, String deviceName) {
        try {
            collection().document(deviceUuid).update("deviceName", deviceName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    public void deleteByDeviceUuid(String deviceUuid) {
        try {
            collection().document(deviceUuid).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIRESTORE_ERROR);
        }
    }

    private Device toDevice(DocumentSnapshot doc) {
        Device device = doc.toObject(Device.class);
        if (device != null) {
            device.setDeviceUuid(doc.getId());
        }
        return device;
    }

    private Device toDevice(QueryDocumentSnapshot doc) {
        Device device = doc.toObject(Device.class);
        device.setDeviceUuid(doc.getId());
        return device;
    }
}
