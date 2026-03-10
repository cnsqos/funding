package com.funding.funding.domain.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
  이메일 인증 코드 엔티티
  - 회원가입 시 6자리 숫자 코드를 이메일로 발송
  - 5분 내 인증 완료해야 함
  - 인증 완료 시 usedAt 세팅, User.emailVerified = true
 */
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;           // 6자리 숫자 코드

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;  // 인증 완료 시각 (null = 미인증)

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public EmailVerificationToken() {}

    // ── 팩토리 메서드 ──────────────────────────────────
    public static EmailVerificationToken create(String email, String code, int expireMinutes) {
        EmailVerificationToken t = new EmailVerificationToken();
        t.email     = email;
        t.code      = code;
        t.expiredAt = LocalDateTime.now().plusMinutes(expireMinutes);
        return t;
    }

    // ── 검증 ──────────────────────────────────────────
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }

    // ── Getter ────────────────────────────────────────
    public Long getId()              { return id; }
    public String getEmail()         { return email; }
    public String getCode()          { return code; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public boolean isVerified()      { return usedAt != null; }
}