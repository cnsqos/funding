package com.funding.funding.domain.user.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Spring Security가 인증 성공 후 담아준 사용자 객체
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 우리 서비스에서 발급한 JWT 토큰 꺼내기
        String accessToken = oAuth2User.getTokenRes().accessToken();
        String refreshToken = oAuth2User.getTokenRes().refreshToken();

        // ─────────────────────────────────────────────
        // refreshToken은 쿠키에 저장
        //
        // 이유:
        // - refreshToken은 오래 살아서 더 민감함
        // - JS에서 직접 접근하지 못하게 HttpOnly 쿠키 사용
        // ─────────────────────────────────────────────
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);

        // JS 접근 차단
        refreshCookie.setHttpOnly(true);

        // 전체 경로에서 사용 가능
        refreshCookie.setPath("/");

        // 7일 보관
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);

        // 개발(localhost)에서는 false
        // 운영(HTTPS)에서는 반드시 true로 변경
        refreshCookie.setSecure(false);

        response.addCookie(refreshCookie);

        // ─────────────────────────────────────────────
        // accessToken은 fragment(#)로 프론트에 전달
        //
        // 왜 queryString(?token=...) 대신 fragment를 쓰나?
        // - queryString은 서버 로그, 브라우저 기록, 리퍼러 등에 남을 수 있음
        // - fragment(#...)는 브라우저가 클라이언트에서만 처리하고
        //   일반적으로 서버로 전송되지 않음
        //
        // 예:
        // http://localhost:3000/oauth/success#accessToken=...
        // ─────────────────────────────────────────────
        String redirectUrl = frontendUrl
                + "/oauth/success#accessToken="
                + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}