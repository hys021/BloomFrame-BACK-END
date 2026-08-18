package com.bloomframe.server.device.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeviceRenameRequest(
        @NotBlank String deviceName
) {}
