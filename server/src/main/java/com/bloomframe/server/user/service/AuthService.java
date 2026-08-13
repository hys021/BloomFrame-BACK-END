package com.bloomframe.server.user.service;

import com.bloomframe.server.common.exception.BusinessException;
import com.bloomframe.server.common.exception.ErrorCode;
import com.bloomframe.server.common.security.JwtTokenProvider;
import com.bloomframe.server.user.dto.request.LoginRequest;
import com.bloomframe.server.user.dto.request.SendCodeRequest;
import com.bloomframe.server.user.dto.request.SignUpRequest;
import com.bloomframe.server.user.dto.request.VerifyCodeRequest;
import com.bloomframe.server.user.dto.response.LoginResponse;
import com.bloomframe.server.user.dto.response.SignUpResponse;
import com.bloomframe.server.user.model.PhoneVerification;
import com.bloomframe.server.user.model.User;
import com.bloomframe.server.user.repository.PhoneVerificationRepository;
import com.bloomframe.server.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private static final int CODE_LENGTH = 6;
    private static final long CODE_VALID_MINUTES = 5;

    private final UserRepository userRepository;
    private final PhoneVerificationRepository phoneVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SmsSender smsSender;

    public AuthService(UserRepository userRepository,
                        PhoneVerificationRepository phoneVerificationRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        SmsSender smsSender) {
        this.userRepository = userRepository;
        this.phoneVerificationRepository = phoneVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.smsSender = smsSender;
    }

    public void sendCode(SendCodeRequest request) {
        String code = generateCode();
        Instant expiresAt = Instant.now().plus(CODE_VALID_MINUTES, ChronoUnit.MINUTES);

        // phone이 문서 ID이므로 save()가 곧 "기존 값 덮어쓰기" -> 재발송 시 최신 코드만 유효
        PhoneVerification verification = new PhoneVerification(request.phone(), code, expiresAt);
        phoneVerificationRepository.save(verification);

        smsSender.send(request.phone(), "[인증번호] " + code + " (5분 이내 입력)");
    }

    public void verifyCode(VerifyCodeRequest request) {
        PhoneVerification verification = phoneVerificationRepository.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_NOT_FOUND));

        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
        }
        if (!verification.getCode().equals(request.code())) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        phoneVerificationRepository.markVerified(request.phone());
    }

    public SignUpResponse signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 대리인 전화번호가 인증 완료 상태인지 확인
        PhoneVerification verification = phoneVerificationRepository.findByPhone(request.caregiverPhone())
                .orElseThrow(() -> new BusinessException(ErrorCode.PHONE_NOT_VERIFIED));

        if (!Boolean.TRUE.equals(verification.getVerified()) || verification.isExpired()) {
            throw new BusinessException(ErrorCode.PHONE_NOT_VERIFIED);
        }

        User user = new User(
                request.name(),
                request.caregiverPhone(),
                request.selfPhone(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        User saved = userRepository.save(user);

        // 가입 완료 후 인증 정보 삭제
        phoneVerificationRepository.deleteByPhone(request.caregiverPhone());

        return SignUpResponse.of(saved.getId());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());
        return LoginResponse.of(token, jwtTokenProvider.getExpirationSeconds());
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
