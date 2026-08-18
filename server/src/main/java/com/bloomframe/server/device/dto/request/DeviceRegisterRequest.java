package com.bloomframe.server.device.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegisterRequest(
        @NotBlank(message = "기기 고유번호는 필수입니다.")
        String deviceUuid
) {
}