package com.funding.funding.domain.user.repository;

import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.entity.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    // 아이디 찾기용
    Optional<User> findByNickname(String nickname);

    // 통계용
    long countByStatus(UserStatus status);
}