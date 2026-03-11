package com.funding.funding.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    // ── 기존 ──────────────────────────────────────────
    public record RegisterReq(
            @Email @NotBlank String email,
            @NotBlank String nickname,
            @NotBlank String password
    ) {}

    public record LoginReq(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record TokenRes(
            String accessToken,
            String refreshToken
    ) {}
    
    public record RefreshReq(
    		@NotBlank String refreshToken
    ) {}

    // ── 이메일 인증 ───────────────────────────────────
    /** 인증 코드 발송 요청 */
    public record SendVerificationReq(
            @Email @NotBlank String email
    ) {}

    /** 인증 코드 확인 요청 */
    public record VerifyEmailReq(
            @Email @NotBlank String email,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자입니다") String code
    ) {}

    // ── 아이디 찾기 ───────────────────────────────────
    /** 닉네임으로 이메일 찾기 */
    public record FindEmailReq(
            @NotBlank String nickname
    ) {}

    /** 아이디 찾기 응답 (마스킹된 이메일 반환) */
    public record FindEmailRes(String maskedEmail) {}

    // ── 비밀번호 찾기 ─────────────────────────────────
    /** 비밀번호 재설정 링크 요청 */
    public record PasswordResetRequestReq(
            @Email @NotBlank String email
    ) {}

    /** 비밀번호 재설정 실행 */
    public record PasswordResetReq(
            @NotBlank String token,
            @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다") String newPassword
    ) {}
    

}