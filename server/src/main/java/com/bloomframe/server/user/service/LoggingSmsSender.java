package com.bloomframe.server.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 개발용 임시 구현체. 실제 문자 대신 로그만 남깁니다.
 * 운영 전, 실제 SMS API 연동 클래스로 교체하세요.
 */
@Service
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String phone, String message) {
        log.info("[SMS 발송] to={}, message={}", phone, message);
    }
}
