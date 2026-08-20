package com.bloomframe.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 프론트(앱/탭)가 백엔드와 다른 origin에 배포될 경우를 대비한 CORS 설정.
 * 로컬 개발 중엔 프론트도 localhost라 문제가 안 드러나지만, 실 배포 시
 * 브라우저가 cross-origin 요청을 기본 차단하기 때문에 필요함.
 *
 * 허용 origin은 application-local.yml에서 app.cors.allowed-origins로 관리 —
 * 실제 배포 도메인 정해지면 그 값만 바꾸면 됨 (코드 수정 불필요).
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:https://bloomframe.vercel.app,http://localhost:3000,http://localhost:5173}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "https://bloomframe.vercel.app",
                "https://bloomframe-*.vercel.app",
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}