package com.bloomframe.server.user.controller;

import com.bloomframe.server.user.dto.request.LoginRequest;
import com.bloomframe.server.user.dto.request.SendCodeRequest;
import com.bloomframe.server.user.dto.request.SignUpRequest;
import com.bloomframe.server.user.dto.request.VerifyCodeRequest;
import com.bloomframe.server.user.dto.response.LoginResponse;
import com.bloomframe.server.user.dto.response.MessageResponse;
import com.bloomframe.server.user.dto.response.SignUpResponse;
import com.bloomframe.server.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public ResponseEntity<MessageResponse> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendCode(request);
        return ResponseEntity.ok(MessageResponse.of("인증번호가 발송되었습니다."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<MessageResponse> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyCode(request);
        return ResponseEntity.ok(MessageResponse.of("인증이 완료되었습니다."));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
