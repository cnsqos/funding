package com.funding.funding.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("크라우드펀딩 플랫폼 API")
                        .description("""
                                ## 크라우드펀딩 플랫폼 REST API 문서
                                
                                ### 인증 방법
                                1. `POST /api/auth/login` 으로 로그인
                                2. 응답의 `data.accessToken` 값 복사
                                3. 우측 상단 **Authorize 🔒** 버튼 클릭
                                4. `Bearer {토큰값}` 입력 후 Authorize
                                
                                ### 권한 구분
                                - **비회원**: 토큰 없이 호출 가능
                                - **회원**: Authorize 후 호출
                                - **관리자**: role=ADMIN 계정으로 로그인 후 호출
                                """)
                        .version("v1.0.0"))
                // ✅ 우측 상단 Authorize 버튼 — JWT 입력창
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 후 발급받은 accessToken을 입력하세요. (Bearer 접두사 불필요)")));
    }
}