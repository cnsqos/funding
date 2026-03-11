package com.funding.funding.domain.user.oauth;

import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.repository.UserRepository;
import com.funding.funding.domain.user.service.auth.AuthService;
import com.funding.funding.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    // 소셜 회원가입 / 로그인 / 토큰 발급 로직 담당
    private final AuthService authService;

    // 소셜 로그인 후 userId 조회용
    private final UserRepository userRepository;

    public CustomOAuth2UserService(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1) Spring 기본 OAuth2UserService를 통해 제공자(카카오/네이버) 사용자 정보 조회
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // registrationId: "kakao", "naver"
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 제공자 원본 응답 attributes
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo;
        String nameAttributeKey;

        // 2) 제공자별 응답 구조가 다르므로 provider별 파싱 클래스 사용
        if ("kakao".equals(registrationId)) {
            userInfo = new KakaoOAuth2UserInfo(attributes);
            nameAttributeKey = "id";

        } else if ("naver".equals(registrationId)) {
            // 네이버는 실제 사용자 정보가 response 안쪽에 들어있음
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            userInfo = new NaverOAuth2UserInfo(response);
            attributes = response;
            nameAttributeKey = "id";

        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다.");
        }

        // 3) 소셜 로그인 처리
        // - 기존 회원이면 로그인
        // - 없으면 자동 회원가입
        // - AccessToken / RefreshToken 발급
        AuthDtos.TokenRes tokenRes = authService.socialLogin(
                userInfo.getProvider(),
                userInfo.getProviderId(),
                userInfo.getEmail(),
                userInfo.getNickname(),
                userInfo.getProfileImage()
        );

        // 4) JWT subject에 넣을 userId 조회
        Long userId = userRepository.findByProviderAndProviderId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "소셜 로그인 사용자 조회에 실패했습니다."))
                .getId();

        // 5) SecurityContext에 들어갈 CustomOAuth2User 반환
        return new CustomOAuth2User(
                userId,
                tokenRes,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                nameAttributeKey
        );
    }
}