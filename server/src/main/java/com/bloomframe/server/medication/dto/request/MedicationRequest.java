package com.bloomframe.server.medication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MedicationRequest(
        @NotBlank(message = "약 이름은 필수입니다.") String name,
        @NotNull @Positive(message = "1일 복용 횟수는 1 이상이어야 합니다.") Integer dosePerDay,
        @NotBlank(message = "복용 타이밍은 필수입니다.") String timing,
        String imageUrl // OCR 원본 이미지 URL, null 허용 (미리 R2에 업로드 후 URL만 전달받는다고 가정)
) {}
