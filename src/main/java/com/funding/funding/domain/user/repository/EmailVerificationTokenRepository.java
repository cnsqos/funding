package com.funding.funding.domain.user.repository;

import com.funding.funding.domain.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    // 이메일 + 코드로 최신 토큰 조회
    Optional<EmailVerificationToken> findTopByEmailOrderByCreatedAtDesc(String email);

    // 이메일 기준 인증 완료 여부 확인
    boolean existsByEmailAndUsedAtIsNotNull(String email);
}