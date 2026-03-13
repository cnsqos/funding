package com.funding.funding.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 생성/검증/파싱 담당
    private final JwtTokenProvider jwt;

    public JwtAuthenticationFilter(JwtTokenProvider jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        // Authorization: Bearer xxx 에서 토큰 추출
        String token = resolveToken(request);

        if (token != null) {
            try {
                // 1) 토큰 서명/만료 검증
                // 2) access token인지 확인, && "access".equals(jwt.getType(token)) 잠시 주석
                if ((jwt.validate(token)) && "access".equals(jwt.getType(token))){

                    Long userId = jwt.getUserId(token);
                    String role = jwt.getRole(token);

                    // 현재 프로젝트는 principal에 userId(Long)를 넣는 구조
                    var auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                    // SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // 토큰이 이상하면 인증 없이 그냥 다음 필터로 넘김
                // 실제 보호된 API 접근 시 SecurityConfig의 인증 처리에서 401 응답
            }
        }

        chain.doFilter(request, response);
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}