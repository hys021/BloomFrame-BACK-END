package com.bloomframe.server.user.controller;

import com.bloomframe.server.common.security.CustomUserPrincipal;
import com.bloomframe.server.user.dto.request.UpdateUserRequest;
import com.bloomframe.server.user.dto.response.UserResponse;
import com.bloomframe.server.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.bloomframe.server.user.dto.request.FcmTokenRequest;
import com.bloomframe.server.user.dto.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return userService.getMe(principal.getUserId());
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @RequestBody UpdateUserRequest request) {
        return userService.updateMe(principal.getUserId(), request);
    }

    // 앱이 로그인 후(또는 FCM 토큰 갱신 시) 호출해서 푸시 발송 대상 토큰을 등록
    @PatchMapping("/me/fcm-token")
    public MessageResponse updateFcmToken(@AuthenticationPrincipal CustomUserPrincipal principal,
                                          @Valid @RequestBody FcmTokenRequest request) {
        userService.updateFcmToken(principal.getUserId(), request);
        return MessageResponse.of("FCM 토큰이 등록되었습니다.");
    }
}
