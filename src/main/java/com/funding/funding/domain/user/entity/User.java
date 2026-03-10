package com.funding.funding.domain.user.entity;

import com.funding.funding.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Getter
    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Getter
    @Column(length = 255)
    private String password;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Getter
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Setter
    @Getter
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Getter
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Getter
    @Column(name = "suspended_reason", length = 255)
    private String suspendedReason;

    @Column(name = "deleted_reason", length = 255)
    private String deletedReason;

    public User() {}

    public User(String email, String nickname, String password, UserRole role, UserStatus status, AuthProvider provider) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.status = status;
        this.provider = provider;
        this.emailVerified = false;
    }

    // 이메일 인증 완료
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    // 비밀번호 변경 (비밀번호 찾기 완료 시)
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 프로필 수정
    public void updateProfile(String nickname, String profileImage) {
        if (nickname != null && !nickname.isBlank()) this.nickname = nickname;
        if (profileImage != null) this.profileImage = profileImage;
    }

    // 관리자 회원 정지 / 활성화
    public void suspend(String reason) {
        this.status = UserStatus.SUSPENDED;
        this.suspendedReason = reason;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.suspendedReason = null;
    }

}