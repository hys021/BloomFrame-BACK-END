package com.bloomframe.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 현재 시각을 Bean으로 주입받게 하기 위한 설정.
 * 서비스 코드에서 Instant.now() 대신 이 Clock을 쓰면, 나중에 테스트할 때
 * 가짜 시각(Clock.fixed(...))으로 바꿔치기해서 "3분 지난 상황"을 쉽게 재현할 수 있다.
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}