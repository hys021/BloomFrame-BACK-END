package com.bloomframe.server.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Auth / User
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "전화번호 인증을 먼저 완료해주세요."),
    VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "인증 요청 내역이 없습니다. 인증번호를 먼저 발송해주세요."),
    VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다. 다시 발송해주세요."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // Device
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "기기를 찾을 수 없습니다."),
    DEVICE_UUID_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 기기입니다."),
    DEVICE_STILL_CONNECTED(HttpStatus.CONFLICT, "연결된 기기는 삭제할 수 없습니다. 먼저 연결을 해제해주세요."),
    DEVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 등록한 기기가 아닙니다."),

    // Medication
    MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "복약 정보를 찾을 수 없습니다."),
    MEDICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 등록한 복약 정보가 아닙니다."),

    // Alarm (medication / exercise / custom 공용)
    MEDICATION_ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "복약 알림을 찾을 수 없습니다."),
    MEDICATION_ALARM_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 등록한 복약 알림이 아닙니다."),
    EXERCISE_ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "운동 알림을 찾을 수 없습니다."),
    EXERCISE_ALARM_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 등록한 운동 알림이 아닙니다."),
    CUSTOM_ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "기타 알림을 찾을 수 없습니다."),
    CUSTOM_ALARM_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 등록한 기타 알림이 아닙니다."),

    // Health Condition
    HEALTH_CONDITION_NOT_FOUND(HttpStatus.NOT_FOUND, "건강 정보를 찾을 수 없습니다."),
    HEALTH_CONDITION_FORBIDDEN(HttpStatus.FORBIDDEN, "본인이 등록한 건강 정보가 아닙니다."),

    // Firestore / Infra
    FIRESTORE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
