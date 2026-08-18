package com.bloomframe.server.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러 파라미터에 붙이면, 인증된 사용자의 uid를 자동으로 주입받는다.
 * 실제로 uid를 어떻게 알아내는지는 AuthenticatedUidArgumentResolver가 담당 —
 * 그 구현체만 나중에 Java #1의 JWT 필터 연동 버전으로 교체하면 되고, 이 어노테이션과
 * 이걸 쓰는 컨트롤러 코드는 그대로 유지된다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedUid {
}