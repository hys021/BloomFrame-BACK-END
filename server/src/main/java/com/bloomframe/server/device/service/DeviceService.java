package com.bloomframe.server.device.service;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.device.dto.request.DeviceRegisterRequest;
import com.bloomframe.server.device.dto.request.DeviceRenameRequest;
import com.bloomframe.server.device.dto.response.DeviceResponse;
import com.bloomframe.server.device.model.Device;
import com.bloomframe.server.device.model.DeviceStatus;
import com.bloomframe.server.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // 액자에 인쇄된 고정 시리얼(deviceUuid)로 기기를 등록하고,
    // 기기 이름은 기본값 "BloomFrame"으로 설정합니다.
    public DeviceResponse register(String userId, DeviceRegisterRequest request) {
        if (deviceRepository.existsByDeviceUuid(request.deviceUuid())) {
            throw new BusinessException(ErrorCode.DEVICE_UUID_DUPLICATE);
        }

        Device device = new Device(
                request.deviceUuid(),
                userId,
                "BloomFrame"
        );

        deviceRepository.save(device);
        return DeviceResponse.from(device);
    }

    public List<DeviceResponse> getDevices(String userId) {
        return deviceRepository.findAllByUserId(userId).stream()
                .map(DeviceResponse::from)
                .toList();
    }

    public DeviceResponse rename(String userId, String deviceUuid, DeviceRenameRequest request) {
        Device device = findOwnedDevice(userId, deviceUuid);
        deviceRepository.updateName(deviceUuid, request.deviceName());
        device.setDeviceName(request.deviceName());
        return DeviceResponse.from(device);
    }

    // 정책: 새 기기 연결 시 기존 CONNECTED 기기는 자동으로 해제된다 (동시 연결 1개 제한)
    public DeviceResponse connect(String userId, String deviceUuid) {
        Device device = findOwnedDevice(userId, deviceUuid);

        deviceRepository.findConnectedByUserId(userId)
                .filter(connected -> !connected.getDeviceUuid().equals(deviceUuid))
                .ifPresent(connected ->
                        deviceRepository.updateStatus(
                                connected.getDeviceUuid(),
                                DeviceStatus.DISCONNECTED
                        )
                );

        deviceRepository.updateStatus(deviceUuid, DeviceStatus.CONNECTED);
        device.setStatus(DeviceStatus.CONNECTED.name());

        return DeviceResponse.from(device);
    }

    public DeviceResponse disconnect(String userId, String deviceUuid) {
        Device device = findOwnedDevice(userId, deviceUuid);
        deviceRepository.updateStatus(deviceUuid, DeviceStatus.DISCONNECTED);
        device.setStatus(DeviceStatus.DISCONNECTED.name());
        return DeviceResponse.from(device);
    }

    // 정책: CONNECTED 상태인 기기는 삭제 불가 (먼저 연결 해제 필요)
    public void delete(String userId, String deviceUuid) {
        Device device = findOwnedDevice(userId, deviceUuid);
        if (device.isConnected()) {
            throw new BusinessException(ErrorCode.DEVICE_STILL_CONNECTED);
        }
        deviceRepository.deleteByDeviceUuid(deviceUuid);
    }

    private Device findOwnedDevice(String userId, String deviceUuid) {
        Device device = deviceRepository.findByDeviceUuid(deviceUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEVICE_NOT_FOUND));

        if (!device.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.DEVICE_FORBIDDEN);
        }
        return device;
    }
}
