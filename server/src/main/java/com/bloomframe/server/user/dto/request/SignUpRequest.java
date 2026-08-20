package com.bloomframe.server.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank(message = "이름은 필수입니다.") String name,

        @NotNull(message = "나이는 필수입니다.")
        @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
        @Max(value = 150, message = "나이는 150 이하여야 합니다.")
        Integer age,

        @NotBlank(message = "대리인 전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다.")
        String caregiverPhone,

        @NotBlank(message = "본인 전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다.")
        String selfPhone,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {}
