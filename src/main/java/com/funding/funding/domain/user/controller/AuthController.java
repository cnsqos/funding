package com.funding.funding.domain.user.controller;

import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.service.auth.AuthService;
import com.funding.funding.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "인증",
        description = "로그인, 회원가입, 이메일 인증, 아이디/비밀번호 찾기, 토큰 재발급, 로그아웃"
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─────────────────────────────────────────────
    // 1) 회원가입
    // - 일반 회원가입 처리
    // - 가입 완료 후 이메일 인증 코드 발송
    // ─────────────────────────────────────────────
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody AuthDtos.RegisterReq req) {
        authService.register(req);
        return ApiResponse.ok("회원가입 완료. 이메일로 인증 코드를 발송했습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 2) 일반 로그인
    // - 이메일/비밀번호 로그인
    // - AccessToken + RefreshToken 발급
    // - RefreshToken은 응답 body에도 있지만,
    //   보안을 위해 HttpOnly 쿠키에도 함께 저장
    //
    // 왜 쿠키로 저장하나?
    // - RefreshToken은 수명이 길고 민감한 토큰
    // - 프론트 JS(localStorage, sessionStorage)보다
    //   HttpOnly 쿠키가 상대적으로 안전함
    // ─────────────────────────────────────────────
    @PostMapping("/login")
    public ApiResponse<AuthDtos.TokenRes> login(HttpServletResponse response,
                                                @Valid @RequestBody AuthDtos.LoginReq req) {
        AuthDtos.TokenRes tokenRes = authService.login(req);

        // refreshToken을 HttpOnly 쿠키로 저장
        addRefreshTokenCookie(response, tokenRes.refreshToken());

        // accessToken / refreshToken 응답 반환
        // 추후 운영에서는 refreshToken을 body에서 빼는 방향도 가능
        return ApiResponse.ok(tokenRes);
    }

    // ─────────────────────────────────────────────
    // 3) AccessToken 재발급
    //
    // refreshToken을 두 가지 방식 중 하나로 받음
    // 1. 요청 body에서 받기
    // 2. body에 없으면 쿠키에서 꺼내기
    //
    // 이렇게 만든 이유
    // - Postman 테스트 편의성 확보
    // - 실제 프론트 운영에서는 쿠키 기반 재발급 가능
    // ─────────────────────────────────────────────
    @PostMapping("/refresh")
    public ApiResponse<AuthDtos.TokenRes> refresh(@RequestBody(required = false) AuthDtos.RefreshReq req,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        String refreshToken = null;

        // body에 refreshToken이 있으면 우선 사용
        if (req != null && req.refreshToken() != null && !req.refreshToken().isBlank()) {
            refreshToken = req.refreshToken();
        } else {
            // body에 없으면 쿠키에서 추출
            refreshToken = extractRefreshTokenFromCookie(request);
        }

        // RefreshToken 검증 후 새 AccessToken 발급
        AuthDtos.TokenRes tokenRes = authService.refresh(refreshToken);

        // RefreshToken 쿠키를 다시 세팅
        // (현재는 동일 토큰 재세팅 수준)
        addRefreshTokenCookie(response, tokenRes.refreshToken());

        return ApiResponse.ok(tokenRes);
    }

    // ─────────────────────────────────────────────
    // 4) 로그아웃
    // - Redis에 저장된 refreshToken 삭제
    // - 브라우저의 refreshToken 쿠키도 삭제
    //
    // 주의:
    // - accessToken은 서버 저장형이 아니라 JWT 자체이므로
    //   서버에서 직접 "삭제"하는 개념은 없음
    // - 대신 refreshToken을 끊어서 재발급을 막음
    // ─────────────────────────────────────────────
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
                                    HttpServletResponse response) {
        Long userId = (Long) authentication.getPrincipal();

        // 서버 측 refreshToken 제거
        authService.logout(userId);

        // 브라우저 측 refreshToken 쿠키 제거
        deleteRefreshTokenCookie(response);

        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 5) 이메일 인증 코드 재발송
    // ─────────────────────────────────────────────
    @PostMapping("/email/send")
    public ApiResponse<Void> sendVerification(@Valid @RequestBody AuthDtos.SendVerificationReq req) {
        authService.sendVerificationCode(req.email());
        return ApiResponse.ok("인증 코드를 발송했습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 6) 이메일 인증 코드 확인
    // ─────────────────────────────────────────────
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody AuthDtos.VerifyEmailReq req) {
        authService.verifyEmail(req);
        return ApiResponse.ok("이메일 인증이 완료되었습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 7) 닉네임으로 이메일(아이디) 찾기
    // ─────────────────────────────────────────────
    @PostMapping("/find-email")
    public ApiResponse<AuthDtos.FindEmailRes> findEmail(@Valid @RequestBody AuthDtos.FindEmailReq req) {
        return ApiResponse.ok(authService.findEmail(req));
    }

    // ─────────────────────────────────────────────
    // 8) 비밀번호 재설정 링크 이메일 발송
    // ─────────────────────────────────────────────
    @PostMapping("/password/reset-request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody AuthDtos.PasswordResetRequestReq req) {
        authService.requestPasswordReset(req);
        return ApiResponse.ok("비밀번호 재설정 링크를 이메일로 발송했습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 9) 비밀번호 재설정 실행
    // ─────────────────────────────────────────────
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.PasswordResetReq req) {
        authService.resetPassword(req);
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }

    // =========================================================
    // 아래부터는 refreshToken 쿠키를 다루는 공통 유틸 메서드
    // =========================================================

    // refreshToken을 HttpOnly 쿠키로 저장
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        // JS(document.cookie)로 접근 못 하게 막음
        // XSS 상황에서 탈취 위험을 줄이는 데 도움
        cookie.setHttpOnly(true);

        // 사이트 전체 경로에서 쿠키 사용 가능
        cookie.setPath("/");

        // 쿠키 수명: 7일
        cookie.setMaxAge(7 * 24 * 60 * 60);

        // 개발(localhost)에서는 false로 둠
        // 운영(HTTPS)에서는 반드시 true로 변경해야 함
        // true면 HTTPS 요청에서만 쿠키 전송
        cookie.setSecure(false);

        response.addCookie(cookie);
    }

    // refreshToken 쿠키 삭제
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);

        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // maxAge=0 이면 즉시 만료 → 브라우저에서 삭제
        cookie.setMaxAge(0);

        // 저장할 때와 같은 secure 설정 흐름 유지
        cookie.setSecure(false);

        response.addCookie(cookie);
    }

    // 요청에 포함된 쿠키들 중 refreshToken 값을 찾아 반환
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}