package com.bloomframe.server.device.model;

import com.google.cloud.firestore.annotation.Exclude;

/**
 * Firestore "devices" 컬렉션 문서 매핑 클래스.
 * 문서 ID로 deviceUuid를 그대로 사용합니다 (DeviceRepository 참고).
 * -> "같은 UUID로 중복 등록"을 Firestore의 문서 생성(create) 자체로 막을 수 있습니다.
 */
public class Device {

    @Exclude
    private String deviceUuid; // 문서 ID와 동일한 값 (조회 편의를 위해 필드로도 보관)

    private String userId;     // 등록한 대리인 users 문서 ID
    private String deviceName;
    private String status;     // DeviceStatus.name() 문자열로 저장 ("CONNECTED" / "DISCONNECTED")

    public Device() {
    }

    public Device(String deviceUuid, String userId, String deviceName) {
        this.deviceUuid = deviceUuid;
        this.userId = userId;
        this.deviceName = deviceName;
        this.status = DeviceStatus.DISCONNECTED.name();
    }

    @Exclude
    public String getDeviceUuid() {
        return deviceUuid;
    }

    @Exclude
    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public boolean isConnected() {
        return DeviceStatus.CONNECTED.name().equals(status);
    }
}
