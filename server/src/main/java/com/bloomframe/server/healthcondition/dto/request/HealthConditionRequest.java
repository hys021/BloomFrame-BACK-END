package com.bloomframe.server.healthcondition.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HealthConditionRequest(
        @NotBlank(message = "건강 상태 이름은 필수입니다.") String conditionName
) {}
