package com.bloomframe.server.common.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Java #1의 JwtAuthenticationFilter가 SecurityContext에 넣어준 CustomUserPrincipal에서
 * userId(=uid)를 꺼낸다.
 */
@Component
public class AuthenticatedUidArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthenticatedUid.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserPrincipal userPrincipal)) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다");
        }
        return userPrincipal.getUserId();
    }
}