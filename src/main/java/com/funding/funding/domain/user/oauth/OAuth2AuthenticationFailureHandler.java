package com.funding.funding.domain.user.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        // 실패 메시지 추출
        String message = extractMessage(exception);

        // 프론트에서 이미 OAuthSuccess 페이지가 #message 처리 로직을 갖고 있다면
        // 실패여도 /oauth/success 로 보내서 화면 처리 가능
        //
        // 예:
        // http://localhost:5173/oauth/success#message=이미%20일반회원으로...
        String redirectUrl = frontendUrl
                + "/oauth/success#message="
                + URLEncoder.encode(message, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    // OAuth2AuthenticationException 안의 실제 description 우선 사용
    private String extractMessage(AuthenticationException exception) {

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            if (oauth2Exception.getError() != null
                    && oauth2Exception.getError().getDescription() != null
                    && !oauth2Exception.getError().getDescription().isBlank()) {
                return oauth2Exception.getError().getDescription();
            }
        }

        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }

        return "소셜 로그인에 실패했습니다.";
    }
}