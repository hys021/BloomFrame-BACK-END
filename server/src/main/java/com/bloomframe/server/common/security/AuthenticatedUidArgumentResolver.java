package com.bloomframe.server.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * ⚠️ 임시 구현. Java #1의 정식 JWT 필터가 나오면 이 클래스 내부만 교체한다.
 * (AuthenticatedUid 어노테이션과 이걸 쓰는 컨트롤러 코드는 그대로 유지됨)
 *
 * 지금은 "X-Debug-Uid" 헤더를 그대로 신뢰해서 uid로 쓴다.
 * 절대 실제 서비스 배포에 이 상태로 나가면 안 됨 — 로컬 테스트 전용.
 */
@Component
public class AuthenticatedUidArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatedUidArgumentResolver.class);

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUid.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String uid = webRequest.getHeader("X-Debug-Uid");
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("X-Debug-Uid 헤더가 필요합니다 (임시 인증 방식, JWT 필터 도입 전까지)");
        }
        log.warn("임시 인증 사용 중 — X-Debug-Uid={} (JWT 필터로 교체 필요)", uid);
        return uid;
    }
}