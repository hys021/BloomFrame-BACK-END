package com.bloomframe.server.user.dto.response;

public record SignUpResponse(
        String userId,
        String message
) {
    public static SignUpResponse of(String userId) {
        return new SignUpResponse(userId, "회원가입이 완료되었습니다.");
    }
}
