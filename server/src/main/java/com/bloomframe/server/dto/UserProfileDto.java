package com.bloomframe.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserProfileDto(
        @NotBlank @Pattern(regexp = "self|representative") String userType,
        @NotBlank String name,
        String phone,
        String birthDate
) {}
