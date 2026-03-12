package com.funding.funding.global.security;

import com.funding.funding.domain.user.oauth.CustomOAuth2UserService;
import com.funding.funding.domain.user.oauth.OAuth2AuthenticationFailureHandler;
import com.funding.funding.domain.user.oauth.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // @PreAuthorize 같은 메서드 단위 권한 제어 활성화
public class SecurityConfig {

    // JWT 인증 필터에서 사용할 토큰 도구
    private final JwtTokenProvider jwt;

    // OAuth2 사용자 정보 조회 + 소셜 로그인 처리 서비스
    private final CustomOAuth2UserService customOAuth2UserService;

    // 소셜 로그인 성공 시 프론트로 토큰 전달
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    // 소셜 로그인 실패 시 실패 처리
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    public SecurityConfig(
            JwtTokenProvider jwt,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler
    ) {
        this.jwt = jwt;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS 기본 설정 사용
                .cors(Customizer.withDefaults())

                // JWT 기반이므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 기본 로그인 창 비활성화
                .httpBasic(b -> b.disable())

                // formLogin 비활성화 (세션 로그인 사용 안 함)
                .formLogin(f -> f.disable())

                // 서버 세션을 사용하지 않는 Stateless 구조
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//              .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 안 된 사용자가 보호된 API 접근 시 401 응답 반환
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                )

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/debug-dashboard.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()

                        // 브라우저 preflight 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 인증 관련 API는 비로그인 상태 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        // OAuth2 진입/콜백 URL 허용
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 공개 조회 API 허용
                        .requestMatchers(HttpMethod.GET, "/api/projects", "/api/projects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}/followers/count").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/{userId}/following/count").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        // 소셜 로그인 시작 URL
                        .authorizationEndpoint(auth -> auth.baseUri("/oauth2/authorization"))

                        // 제공자 인증 후 콜백 받는 URL
                        .redirectionEndpoint(redir -> redir.baseUri("/login/oauth2/code/*"))

                        // 제공자에서 사용자 정보 조회 후 우리 서비스 로직 수행
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))

                        // 성공/실패 핸들러 지정
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )

                // UsernamePasswordAuthenticationFilter 전에 JWT 필터를 먼저 태움
                .addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class);

        // H2 콘솔 iframe 허용
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}