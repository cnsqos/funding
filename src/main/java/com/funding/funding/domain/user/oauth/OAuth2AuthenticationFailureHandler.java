package com.funding.funding.domain.user.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException, ServletException {

        // 인증 실패 메시지 정리
        String message = exception.getMessage() == null
                ? "소셜 로그인에 실패했습니다."
                : exception.getMessage();

        // ─────────────────────────────────────────────
        // 실패 메시지도 queryString 대신 fragment(#)로 전달
        //
        // 예:
        // http://localhost:3000/oauth/failure#message=...
        //
        // 프론트에서는 window.location.hash 로 읽으면 됨
        // ─────────────────────────────────────────────
        String redirectUrl = frontendUrl
                + "/oauth/failure#message="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}