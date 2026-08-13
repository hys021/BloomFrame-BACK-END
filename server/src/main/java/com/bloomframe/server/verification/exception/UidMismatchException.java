package com.bloomframe.server.verification.exception;

/**
 * URL 경로의 {uid}와 인증된 사용자의 실제 uid가 일치하지 않을 때.
 * 다른 사람의 데이터를 조회하려는 시도이므로 403으로 응답한다.
 */
public class UidMismatchException extends RuntimeException {
    public UidMismatchException() {
        super("경로의 uid와 인증된 사용자가 일치하지 않습니다");
    }
}