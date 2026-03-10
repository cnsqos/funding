package com.funding.funding.domain.user.service.auth;

import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.entity.*;
import com.funding.funding.domain.user.repository.*;
import com.funding.funding.domain.user.service.email.EmailService;
import com.funding.funding.global.exception.ApiException;
import com.funding.funding.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository                  userRepository;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository    passwordResetTokenRepository;
    private final PasswordEncoder                 passwordEncoder;
    private final JwtTokenProvider                jwt;
    private final EmailService                    emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository,
                       EmailVerificationTokenRepository emailTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwt,
                       EmailService emailService) {
        this.userRepository           = userRepository;
        this.emailTokenRepository     = emailTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder          = passwordEncoder;
        this.jwt                      = jwt;
        this.emailService             = emailService;
    }

    // ── 회원가입 ──────────────────────────────────────
    @Transactional
    public void register(AuthDtos.RegisterReq req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(req.nickname())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        User user = new User(
                req.email(), req.nickname(),
                passwordEncoder.encode(req.password()),
                UserRole.USER, UserStatus.ACTIVE, AuthProvider.LOCAL
        );
        userRepository.save(user);

        // 회원가입 완료 → 인증 코드 자동 발송
        sendVerificationCode(req.email());
    }

    // ── 로그인 ────────────────────────────────────────
    public AuthDtos.TokenRes login(AuthDtos.LoginReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPassword() == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "활성 상태의 계정이 아닙니다.");
        }

        String access = jwt.createAccessToken(user.getId(), user.getRole().name());
        return new AuthDtos.TokenRes(access);
    }

    // ── 이메일 인증: 코드 발송 ────────────────────────
    @Transactional
    public void sendVerificationCode(String email) {
        // 가입된 이메일인지 확인
        if (!userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다.");
        }

        String code = generateSixDigitCode();
        EmailVerificationToken token = EmailVerificationToken.create(email, code, 5); // 5분 유효
        emailTokenRepository.save(token);

        emailService.sendVerificationCode(email, code); // 비동기 발송
    }

    // ── 이메일 인증: 코드 확인 ────────────────────────
    @Transactional
    public void verifyEmail(AuthDtos.VerifyEmailReq req) {
        EmailVerificationToken token = emailTokenRepository
                .findTopByEmailOrderByCreatedAtDesc(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "인증 코드를 먼저 요청해주세요."));

        if (token.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 사용된 인증 코드입니다.");
        }
        if (token.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        if (!token.getCode().equals(req.code())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다.");
        }

        token.markUsed();

        // User.emailVerified = true 처리
        userRepository.findByEmail(req.email()).ifPresent(User::verifyEmail);
    }

    // ── 아이디 찾기 ───────────────────────────────────
    public AuthDtos.FindEmailRes findEmail(AuthDtos.FindEmailReq req) {
        User user = userRepository.findByNickname(req.nickname())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "해당 닉네임의 회원을 찾을 수 없습니다."));

        String masked = maskEmail(user.getEmail());

        // 마스킹된 이메일을 해당 이메일로도 발송 (선택적으로 제거 가능)
        emailService.sendFoundEmail(user.getEmail(), masked);

        return new AuthDtos.FindEmailRes(masked);
    }

    // ── 비밀번호 찾기: 재설정 링크 발송 ──────────────
    @Transactional
    public void requestPasswordReset(AuthDtos.PasswordResetRequestReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다."));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.create(user, token, 30); // 30분 유효
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetLink(user.getEmail(), token, frontendUrl);
    }

    // ── 비밀번호 재설정 실행 ──────────────────────────
    @Transactional
    public void resetPassword(AuthDtos.PasswordResetReq req) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(req.token())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "유효하지 않은 토큰입니다."));

        if (resetToken.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 사용된 토큰입니다.");
        }
        if (resetToken.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "만료된 토큰입니다. 비밀번호 찾기를 다시 시도해주세요.");
        }

        resetToken.markUsed();
        resetToken.getUser().changePassword(passwordEncoder.encode(req.newPassword()));
    }

    // ── 유틸 ──────────────────────────────────────────
    private String generateSixDigitCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    /**
     * 이메일 마스킹: test@example.com → te**@example.com
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return email; // 앞부분이 너무 짧으면 그대로

        String local  = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String masked = local.substring(0, 2) + "*".repeat(local.length() - 2);
        return masked + domain;
    }
}