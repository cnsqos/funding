package com.funding.funding.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ─────────────────────────────────────────────
        // 프론트 React 서버 주소 허용
        // 개발 환경 기준: localhost:3000
        //
        // 운영으로 가면:
        // https://your-frontend-domain.com
        // 이런 식으로 바꿔야 함
        // ─────────────────────────────────────────────
        config.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 허용할 요청 헤더
        // Authorization, Content-Type 등 포함 가능
        config.setAllowedHeaders(List.of("*"));

        // 쿠키 포함 요청 허용
        //
        // 이게 true여야 브라우저가
        // refreshToken 쿠키를 함께 보낼 수 있음
        //
        // 예:
        // fetch(..., { credentials: "include" })
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모든 경로에 대해 위 CORS 설정 적용
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}