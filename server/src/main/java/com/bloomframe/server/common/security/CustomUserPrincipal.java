package com.bloomframe.server.common.security;

/**
 * JWT에서 추출한 인증 사용자 정보.
 * userId는 Firestore users 컬렉션의 문서 ID(String)입니다.
 */
public class CustomUserPrincipal {

    private final String userId;
    private final String email;

    public CustomUserPrincipal(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
