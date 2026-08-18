package com.bloomframe.server.user.model;

import com.google.cloud.firestore.annotation.Exclude;

import java.time.Instant;

/**
 * Firestore "phoneVerifications" 컬렉션 문서 매핑 클래스.
 * 문서 ID로 phone(전화번호) 값을 그대로 사용합니다 (PhoneVerificationRepository 참고).
 */
public class PhoneVerification {

    @Exclude
    private String phone; // 문서 ID와 동일한 값 (조회 편의를 위해 필드로도 보관)

    private String code;
    private Boolean verified;
    private Long expiresAtEpochMillis; // Firestore Timestamp 대신 epoch millis로 단순 저장

    public PhoneVerification() {
    }

    public PhoneVerification(String phone, String code, Instant expiresAt) {
        this.phone = phone;
        this.code = code;
        this.verified = false;
        this.expiresAtEpochMillis = expiresAt.toEpochMilli();
    }

    @Exclude
    public String getPhone() {
        return phone;
    }

    @Exclude
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Long getExpiresAtEpochMillis() {
        return expiresAtEpochMillis;
    }

    public void setExpiresAtEpochMillis(Long expiresAtEpochMillis) {
        this.expiresAtEpochMillis = expiresAtEpochMillis;
    }

    @Exclude
    public boolean isExpired() {
        return Instant.now().toEpochMilli() > expiresAtEpochMillis;
    }
}
