package com.funding.funding.domain.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.service.auth.AuthService;
import com.funding.funding.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인, 회원가입, 이메일 인증, 아이디/비밀번호 찾기")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── 기존 ──────────────────────────────────────────

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody AuthDtos.RegisterReq req) {
        authService.register(req);
        return ApiResponse.ok("회원가입 완료. 이메일로 인증 코드를 발송했습니다.", null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.TokenRes> login(@Valid @RequestBody AuthDtos.LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    // ── 이메일 인증 ───────────────────────────────────

    // 인증 코드 재발송 (회원가입 후 코드 만료됐을 때)
    @PostMapping("/email/send")
    public ApiResponse<Void> sendVerification(@Valid @RequestBody AuthDtos.SendVerificationReq req) {
        authService.sendVerificationCode(req.email());
        return ApiResponse.ok("인증 코드를 발송했습니다.", null);
    }

    // 인증 코드 확인
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody AuthDtos.VerifyEmailReq req) {
        authService.verifyEmail(req);
        return ApiResponse.ok("이메일 인증이 완료되었습니다.", null);
    }

    // ── 아이디 찾기 ───────────────────────────────────

    // 닉네임으로 이메일(아이디) 찾기
    @PostMapping("/find-email")
    public ApiResponse<AuthDtos.FindEmailRes> findEmail(@Valid @RequestBody AuthDtos.FindEmailReq req) {
        return ApiResponse.ok(authService.findEmail(req));
    }

    // ── 비밀번호 찾기 ─────────────────────────────────

    // 비밀번호 재설정 링크 이메일 발송
    @PostMapping("/password/reset-request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody AuthDtos.PasswordResetRequestReq req) {
        authService.requestPasswordReset(req);
        return ApiResponse.ok("비밀번호 재설정 링크를 이메일로 발송했습니다.", null);
    }

    // 비밀번호 재설정 실행
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.PasswordResetReq req) {
        authService.resetPassword(req);
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }
}