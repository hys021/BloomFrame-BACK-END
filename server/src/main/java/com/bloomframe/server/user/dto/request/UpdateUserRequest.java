package com.bloomframe.server.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 마이페이지 부분 수정용. 넘어온 필드만 반영 (null이면 미변경)
public record UpdateUserRequest(
        String name,

        @Min(value = 1, message = "나이는 1 이상이어야 합니다.")
        @Max(value = 150, message = "나이는 150 이하여야 합니다.")
        Integer age,

        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다.")
        String selfPhone,

        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다.")
        String caregiverPhone,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {}