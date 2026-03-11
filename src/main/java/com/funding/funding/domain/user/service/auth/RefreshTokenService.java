package com.funding.funding.domain.user.service.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenService {

    // Redis key prefix
    private static final String PREFIX = "RT:";

    // 문자열 전용 RedisTemplate
    // Spring Boot가 자동으로 StringRedisTemplate 빈을 만들어줌
    private final StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // RefreshToken 저장
    public void save(Long userId, String refreshToken, long expDays) {
        redisTemplate.opsForValue().set(
                PREFIX + userId,
                refreshToken,
                Duration.ofDays(expDays)
        );
    }

    // 저장된 RefreshToken 조회
    public String get(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    // 로그아웃 시 RefreshToken 삭제
    public void delete(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}