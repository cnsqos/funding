package com.funding.funding.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    // ── 회원가입 ─────────────────────────────────────
    // 비밀번호는 8자 이상 20자 이하로 제한
    public record RegisterReq(
            @Email
            @NotBlank(message = "이메일은 필수입니다.")
            String email,

            @NotBlank(message = "닉네임은 필수입니다.")
            String nickname,

            @NotBlank(message = "비밀번호는 필수입니다.")
            @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
            String password
    ) {}

    // ── 로그인 ───────────────────────────────────────
    public record LoginReq(
            @Email
            @NotBlank(message = "이메일은 필수입니다.")
            String email,

            @NotBlank(message = "비밀번호는 필수입니다.")
            String password
    ) {}

    // ── 로그인 / 재발급 응답 ─────────────────────────
    // accessToken: 실제 API 인증에 사용하는 토큰
    // refreshToken: accessToken 재발급에 사용하는 토큰
    public record TokenRes(
            String accessToken,
            String refreshToken
    ) {}

    // ── 토큰 재발급 요청 ─────────────────────────────
    // body로 refreshToken을 보내는 경우 사용
    public record RefreshReq(
            @NotBlank(message = "RefreshToken은 필수입니다.")
            String refreshToken
    ) {}

    // ── 회원 탈퇴 요청 ───────────────────────────────
    // LOCAL 계정은 currentPassword 검증
    // KAKAO / NAVER 계정은 currentPassword 없이 탈퇴 가능
    public record WithdrawReq(
            String currentPassword,

            @Size(max = 255, message = "탈퇴 사유는 255자 이하로 입력해주세요.")
            String reason
    ) {}

    // ── 이메일 인증 ───────────────────────────────────
    /** 인증 코드 발송 요청 */
    public record SendVerificationReq(
            @Email
            @NotBlank(message = "이메일은 필수입니다.")
            String email
    ) {}

    /** 인증 코드 확인 요청 */
    public record VerifyEmailReq(
            @Email
            @NotBlank(message = "이메일은 필수입니다.")
            String email,

            @NotBlank(message = "인증 코드는 필수입니다.")
            @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자입니다")
            String code
    ) {}

    // ── 아이디 찾기 ───────────────────────────────────
    /** 닉네임으로 이메일 찾기 */
    public record FindEmailReq(
            @NotBlank(message = "닉네임은 필수입니다.")
            String nickname
    ) {}

    /** 아이디 찾기 응답 (마스킹된 이메일 반환) */
    public record FindEmailRes(String maskedEmail) {}

    // ── 비밀번호 찾기 ─────────────────────────────────
    /** 비밀번호 재설정 링크 요청 */
    public record PasswordResetRequestReq(
            @Email
            @NotBlank(message = "이메일은 필수입니다.")
            String email
    ) {}

    /** 비밀번호 재설정 실행 */
    public record PasswordResetReq(
            @NotBlank(message = "토큰은 필수입니다.")
            String token,

            // 회원가입과 동일하게 8자 이상 20자 이하로 통일
            @NotBlank(message = "새 비밀번호는 필수입니다.")
            @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
            String newPassword
    ) {}
}