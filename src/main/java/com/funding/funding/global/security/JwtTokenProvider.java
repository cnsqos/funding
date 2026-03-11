package com.funding.funding.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // JWT 서명에 사용할 SecretKey
    private final SecretKey key;

    // AccessToken 만료 시간(ms)
    private final long accessExpMs;

    // RefreshToken 만료 시간(ms)
    private final long refreshExpMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-exp-minutes}") long accessExpMinutes,
            @Value("${jwt.refresh-token-exp-days}") long refreshExpDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = accessExpMinutes * 60_000L;
        this.refreshExpMs = refreshExpDays * 24 * 60 * 60 * 1000L;
    }

    // AccessToken 생성
    // - subject: userId
    // - role: 권한
    // - type: access
    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    // RefreshToken 생성
    // - subject: userId
    // - type: refresh
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    // 토큰 유효성 검증
    // - 서명, 만료시간 등을 확인
    public boolean validate(String token) {
        Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
        return true;
    }

    // 토큰 subject에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.valueOf(claims.getSubject());
    }

    // AccessToken에 담긴 role 추출
    public String getRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    // 토큰 타입(access / refresh) 추출
    public String getType(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object type = claims.get("type");
        return type == null ? null : type.toString();
    }
}