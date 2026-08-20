package com.bloomframe.server.user.service;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.user.dto.request.UpdateUserRequest;
import com.bloomframe.server.user.dto.response.UserResponse;
import com.bloomframe.server.user.model.PhoneVerification;
import com.bloomframe.server.user.model.User;
import com.bloomframe.server.user.repository.PhoneVerificationRepository;
import com.bloomframe.server.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PhoneVerificationRepository phoneVerificationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.phoneVerificationRepository = phoneVerificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getMe(String userId) {
        return UserResponse.from(findUser(userId));
    }

    public UserResponse updateMe(String userId, UpdateUserRequest request) {

        User currentUser = findUser(userId);

        List<String> fields = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        if (request.name() != null) {
            fields.add("name");
            values.add(request.name());
        }

        if (request.age() != null) {
            fields.add("age");
            values.add(request.age());
        }

        if (request.selfPhone() != null) {
            fields.add("selfPhone");
            values.add(request.selfPhone());
        }

        boolean caregiverPhoneChanged = false;

        // 대리인 전화번호가 실제로 변경되는 경우에만 인증 확인
        if (request.caregiverPhone() != null
                && !request.caregiverPhone().equals(currentUser.getCaregiverPhone())) {

            PhoneVerification verification =
                    phoneVerificationRepository.findByPhone(request.caregiverPhone())
                            .orElseThrow(() ->
                                    new BusinessException(ErrorCode.PHONE_NOT_VERIFIED));

            if (!Boolean.TRUE.equals(verification.getVerified())
                    || verification.isExpired()) {
                throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
            }

            fields.add("caregiverPhone");
            values.add(request.caregiverPhone());

            caregiverPhoneChanged = true;
        }

        // 이메일이 실제로 변경되는 경우에만 중복 검사
        if (request.email() != null
                && !request.email().equals(currentUser.getEmail())) {

            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }

            fields.add("email");
            values.add(request.email());
        }

        // 비밀번호는 반드시 암호화해서 저장
        if (request.password() != null) {
            fields.add("password");
            values.add(passwordEncoder.encode(request.password()));
        }

        if (!fields.isEmpty()) {
            userRepository.update(userId, fields, values);
        }

        // 사용한 인증정보 삭제
        if (caregiverPhoneChanged) {
            phoneVerificationRepository.deleteByPhone(request.caregiverPhone());
        }

        return UserResponse.from(findUser(userId));
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}