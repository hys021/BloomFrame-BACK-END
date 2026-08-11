package com.bloomframe.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeviceRegistrationDto(
        @NotBlank @Pattern(regexp = "android|pc") String platform,
        @NotBlank String fcmToken
) {}
