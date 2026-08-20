package com.bloomframe.server.user.dto.response;

public record SendCodeResponse(
        String message,
        String verificationCode
) {
    public static SendCodeResponse of(String message, String verificationCode) {
        return new SendCodeResponse(message, verificationCode);
    }
}