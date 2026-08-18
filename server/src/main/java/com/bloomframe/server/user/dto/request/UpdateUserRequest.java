package com.bloomframe.server.user.dto.request;

// 마이페이지 부분 수정용. 넘어온 필드만 반영 (null이면 미변경)
public record UpdateUserRequest(
        String name,
        String selfPhone
) {}
