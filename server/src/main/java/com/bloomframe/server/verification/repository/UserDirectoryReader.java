package com.bloomframe.server.verification.repository;

import java.util.List;

/**
 * users 컬렉션의 uid 목록만 읽는 읽기 전용 접근.
 * 소유권은 Java #1(회원 정보). verification 스케줄러가 "전체 유저를 순회"해야 해서 필요.
 *
 * 참고: 지금은 시연/해커톤 규모라 전체 스캔 방식으로 충분하지만,
 * 유저 수가 커지면 비효율적이라는 걸 인지하고 있음 (TODO: 필요시 최적화).
 */
public interface UserDirectoryReader {
    List<String> findAllUids();
}