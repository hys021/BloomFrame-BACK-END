package com.bloomframe.server.user.service;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.user.dto.request.FcmTokenRequest;
import com.bloomframe.server.user.dto.request.UpdateUserRequest;
import com.bloomframe.server.user.dto.response.UserResponse;
import com.bloomframe.server.user.model.User;
import com.bloomframe.server.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getMe(String userId) {
        return UserResponse.from(findUser(userId));
    }

    public UserResponse updateMe(String userId, UpdateUserRequest request) {
        // 넘어온 필드만 골라서 부분 업데이트 (Firestore update()는 지정한 필드만 변경, 나머지는 그대로 유지)
        List<String> fields = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        if (request.name() != null) {
            fields.add("name");
            values.add(request.name());
        }
        if (request.selfPhone() != null) {
            fields.add("selfPhone");
            values.add(request.selfPhone());
        }

        if (!fields.isEmpty()) {
            userRepository.update(userId, fields, values);
        }

        return UserResponse.from(findUser(userId));
    }

    // 앱이 로그인 후(또는 토큰 갱신 시) 이 사용자의 폰 FCM 토큰을 등록/갱신
    public void updateFcmToken(String userId, FcmTokenRequest request) {
        findUser(userId); // 존재하지 않는 유저면 여기서 예외
        userRepository.update(userId, List.of("fcmToken"), List.of(request.fcmToken()));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
