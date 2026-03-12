package com.funding.funding.domain.user.oauth;

import com.funding.funding.global.exception.ApiException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
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
                                        AuthenticationException exception)
            throws IOException, ServletException {

        // 인증 실패 메시지 정리
        String message = extractMessage(exception);

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

    // 중첩된 예외 안에 들어있는 실제 ApiException 메시지까지 추적
    private String extractMessage(AuthenticationException exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof ApiException apiException) {
                return apiException.getMessage();
            }

            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                // ApiException이 아니더라도 의미 있는 메시지가 있으면 후보로 사용
                String msg = current.getMessage();

                // Spring Security 기본 문구처럼 너무 추상적인 건 마지막 fallback으로 넘기기
                if (!"authentication failed".equalsIgnoreCase(msg.trim())
                        && !"OAuth2 Authentication failed".equalsIgnoreCase(msg.trim())) {
                    return msg;
                }
            }

            current = current.getCause();
        }

        return "소셜 로그인에 실패했습니다.";
    }
}