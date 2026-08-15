package com.bloomframe.server.user.controller;

import com.bloomframe.server.common.security.CustomUserPrincipal;
import com.bloomframe.server.user.dto.request.UpdateUserRequest;
import com.bloomframe.server.user.dto.response.UserResponse;
import com.bloomframe.server.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
