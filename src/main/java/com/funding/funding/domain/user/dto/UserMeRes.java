package com.funding.funding.domain.user.dto;

import com.funding.funding.domain.user.entity.AuthProvider;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.entity.UserRole;
import com.funding.funding.domain.user.entity.UserStatus;

import java.time.LocalDateTime;

public record UserMeRes(
        Long id,
        String email,
        String nickname,
        UserRole role,
        UserStatus status,
        AuthProvider provider,
        boolean emailVerified,
        String profileImage,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public static UserMeRes from(User user) {
        return new UserMeRes(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getProvider(),
                user.isEmailVerified(),
                user.getProfileImage(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}