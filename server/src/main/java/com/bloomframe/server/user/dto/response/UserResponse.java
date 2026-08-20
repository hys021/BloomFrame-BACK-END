package com.bloomframe.server.user.dto.response;

import com.bloomframe.server.user.model.User;

public record UserResponse(
        String id,
        String name,
        Integer age,
        String caregiverPhone,
        String selfPhone,
        String email
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getCaregiverPhone(),
                user.getSelfPhone(),
                user.getEmail()
        );
    }
}
