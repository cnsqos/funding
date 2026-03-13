package com.funding.funding.domain.user.entity;

import com.funding.funding.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
public class User extends BaseTimeEntity {

    // 회원 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 이메일
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // 닉네임
    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    // LOCAL 회원 비밀번호
    // 소셜 회원(KAKAO / NAVER)은 null 가능
    @Column(length = 255)
    private String password;

    // 권한(USER / ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // 계정 상태(ACTIVE / SUSPENDED / DELETED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    // 가입 방식(LOCAL / KAKAO / NAVER / GUEST)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    // 소셜 로그인 제공자 고유 ID
    @Column(name = "provider_id", length = 255)
    private String providerId;

    // 이메일 인증 여부
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    // 이메일 인증 완료 시각
    @Setter
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    // 프로필 이미지 URL
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    // 마지막 로그인 시각
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 정지 사유
    @Column(name = "suspended_reason", length = 255)
    private String suspendedReason;

    // 탈퇴 사유
    @Column(name = "deleted_reason", length = 255)
    private String deletedReason;

    // JPA 기본 생성자
    protected User() {}

    // 일반 회원 생성자
    public User(String email, String nickname, String password, UserRole role, UserStatus status, AuthProvider provider) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.status = status;
        this.provider = provider;
        this.emailVerified = false;
    }

    // 소셜 회원 생성 팩토리 메서드
    public static User createSocialUser(
            String email,
            String nickname,
            AuthProvider provider,
            String providerId,
            String profileImage
    ) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.password = null; // 소셜 회원은 비밀번호 없음
        user.role = UserRole.USER;
        user.status = UserStatus.ACTIVE;
        user.provider = provider;
        user.providerId = providerId;
        user.profileImage = profileImage;
        user.emailVerified = true; // 소셜 제공자 이메일 정보는 신뢰
        user.emailVerifiedAt = LocalDateTime.now();
        user.lastLoginAt = LocalDateTime.now();
        return user;
    }

    // 이메일 인증 완료 처리
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    // 비밀번호 변경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 프로필 수정
    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    // 소셜 로그인 시 최신 정보 반영
    public void updateSocialInfo(String nickname, String profileImage, String providerId) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (profileImage != null && !profileImage.isBlank()) {
            this.profileImage = profileImage;
        }
        if (providerId != null && !providerId.isBlank()) {
            this.providerId = providerId;
        }
        this.lastLoginAt = LocalDateTime.now();
    }

    // 일반 로그인 시 마지막 로그인 시각 갱신
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    // 관리자 회원 정지
    public void suspend(String reason) {
        this.status = UserStatus.SUSPENDED;
        this.suspendedReason = reason;
    }

    // 관리자 회원 정지 해제
    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.suspendedReason = null;
    }

    // 회원 탈퇴 (소프트 삭제)
    // 실제 DB 삭제가 아니라 상태만 DELETED로 변경
    public void withdraw(String reason) {
        this.status = UserStatus.DELETED;
        this.deletedReason = reason;
    }
}