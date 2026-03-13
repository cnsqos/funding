package com.funding.funding.domain.user.controller;

import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.service.auth.AuthService;
import com.funding.funding.global.exception.ApiException;
import com.funding.funding.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "인증",
        description = "로그인, 회원가입, 이메일 인증, 아이디/비밀번호 찾기, 토큰 재발급, 로그아웃, 회원 탈퇴"
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
    // ─────────────────────────────────────────────
    @PostMapping("/login")
    public ApiResponse<AuthDtos.TokenRes> login(HttpServletResponse response,
                                                @Valid @RequestBody AuthDtos.LoginReq req) {
        AuthDtos.TokenRes tokenRes = authService.login(req);

        // refreshToken을 HttpOnly 쿠키로 저장
        addRefreshTokenCookie(response, tokenRes.refreshToken());

        return ApiResponse.ok(tokenRes);
    }

    // ─────────────────────────────────────────────
    // 3) AccessToken 재발급
    // - body에 refreshToken이 있으면 우선 사용
    // - 없으면 쿠키에서 refreshToken 추출
    // ─────────────────────────────────────────────
    @PostMapping("/refresh")
    public ApiResponse<AuthDtos.TokenRes> refresh(@RequestBody(required = false) AuthDtos.RefreshReq req,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        String refreshToken;

        // body 우선
        if (req != null && req.refreshToken() != null && !req.refreshToken().isBlank()) {
            refreshToken = req.refreshToken();
        } else {
            // body에 없으면 쿠키에서 추출
            refreshToken = extractRefreshTokenFromCookie(request);
        }

        AuthDtos.TokenRes tokenRes = authService.refresh(refreshToken);

        // refreshToken 쿠키 재세팅
        addRefreshTokenCookie(response, tokenRes.refreshToken());

        return ApiResponse.ok(tokenRes);
    }

    // ─────────────────────────────────────────────
    // 4) 로그아웃
    // - Redis에 저장된 refreshToken 삭제
    // - 브라우저 refreshToken 쿠키도 삭제
    // ─────────────────────────────────────────────
    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication,
                                    HttpServletResponse response) {

        // 인증 객체에서 안전하게 userId 추출
        Long userId = extractUserId(authentication);

        // 서버 측 refreshToken 제거
        authService.logout(userId);

        // 브라우저 측 refreshToken 쿠키 제거
        deleteRefreshTokenCookie(response);

        return ApiResponse.ok("로그아웃 되었습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 5) 회원 탈퇴
    // - LOCAL 계정: 현재 비밀번호 확인 후 탈퇴
    // - 소셜 계정: 비밀번호 없이 탈퇴 가능
    // - 탈퇴 시 refreshToken도 함께 제거
    // ─────────────────────────────────────────────
    @PostMapping("/withdraw")
    public ApiResponse<Void> withdraw(Authentication authentication,
                                      HttpServletResponse response,
                                      @Valid @RequestBody AuthDtos.WithdrawReq req) {

        // 인증 객체에서 안전하게 userId 추출
        Long userId = extractUserId(authentication);

        // 서버 측 탈퇴 처리
        authService.withdraw(userId, req);

        // 브라우저 refreshToken 쿠키도 삭제
        deleteRefreshTokenCookie(response);

        return ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 6) 이메일 인증 코드 재발송
    // ─────────────────────────────────────────────
    @PostMapping("/email/send")
    public ApiResponse<Void> sendVerification(@Valid @RequestBody AuthDtos.SendVerificationReq req) {
        authService.sendVerificationCode(req.email());
        return ApiResponse.ok("인증 코드를 발송했습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 7) 이메일 인증 코드 확인
    // ─────────────────────────────────────────────
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody AuthDtos.VerifyEmailReq req) {
        authService.verifyEmail(req);
        return ApiResponse.ok("이메일 인증이 완료되었습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 8) 닉네임으로 이메일(아이디) 찾기
    // ─────────────────────────────────────────────
    @PostMapping("/find-email")
    public ApiResponse<AuthDtos.FindEmailRes> findEmail(@Valid @RequestBody AuthDtos.FindEmailReq req) {
        return ApiResponse.ok(authService.findEmail(req));
    }

    // ─────────────────────────────────────────────
    // 9) 비밀번호 재설정 링크 이메일 발송
    // ─────────────────────────────────────────────
    @PostMapping("/password/reset-request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody AuthDtos.PasswordResetRequestReq req) {
        authService.requestPasswordReset(req);
        return ApiResponse.ok("비밀번호 재설정 링크를 이메일로 발송했습니다.", null);
    }

    // ─────────────────────────────────────────────
    // 10) 비밀번호 재설정 실행
    // ─────────────────────────────────────────────
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody AuthDtos.PasswordResetReq req) {
        authService.resetPassword(req);
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }

    // =========================================================
    // 아래부터는 refreshToken 쿠키 공통 유틸
    // =========================================================

    // refreshToken을 HttpOnly 쿠키로 저장
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        // JS(document.cookie)로 접근 불가
        cookie.setHttpOnly(true);

        // 사이트 전체 경로에서 사용
        cookie.setPath("/");

        // 7일 유지
        cookie.setMaxAge(7 * 24 * 60 * 60);

        // 개발(localhost)에서는 false
        // 운영(HTTPS)에서는 반드시 true
        cookie.setSecure(false);

        response.addCookie(cookie);
    }

    // refreshToken 쿠키 삭제
    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);

        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // 즉시 만료
        cookie.setMaxAge(0);
        cookie.setSecure(false);

        response.addCookie(cookie);
    }

    // 쿠키에서 refreshToken 추출
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

    // Authentication 객체에서 현재 로그인 사용자 ID를 안전하게 추출
    // - 인증 정보가 없으면 401 반환
    // - principal 타입이 Long 또는 String인 경우만 허용
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Object principal = authentication.getPrincipal();

        // JwtAuthenticationFilter에서 principal을 Long으로 넣는 경우
        if (principal instanceof Long userId) {
            return userId;
        }

        // 혹시 문자열로 들어오는 경우도 방어
        if (principal instanceof String str) {
            try {
                return Long.valueOf(str);
            } catch (NumberFormatException e) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
            }
        }

        throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }
}