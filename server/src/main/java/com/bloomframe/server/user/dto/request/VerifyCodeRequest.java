package com.bloomframe.server.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank String phone,
        @NotBlank String code
) {}
