package com.bloomframe.server.device.dto.response;

import com.bloomframe.server.device.model.Device;

public record DeviceResponse(
        String deviceUuid,
        String deviceName,
        String status
) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(device.getDeviceUuid(), device.getDeviceName(), device.getStatus());
    }
}
