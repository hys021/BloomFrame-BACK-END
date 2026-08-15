package com.bloomframe.server.user.service;

/**
 * SMS 발송 추상화 인터페이스.
 * 실제 구현체는 NCP SENS / Coolsms 등 사용 벤더에 맞춰 별도 클래스로 만들고 Bean 등록하세요.
 */
public interface SmsSender {
    void send(String phone, String message);
}
