package com.funding.funding.domain.user.service.auth;

import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.entity.AuthProvider;
import com.funding.funding.domain.user.entity.EmailVerificationToken;
import com.funding.funding.domain.user.entity.PasswordResetToken;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.entity.UserRole;
import com.funding.funding.domain.user.entity.UserStatus;
import com.funding.funding.domain.user.repository.EmailVerificationTokenRepository;
import com.funding.funding.domain.user.repository.PasswordResetTokenRepository;
import com.funding.funding.domain.user.repository.UserRepository;
import com.funding.funding.domain.user.service.email.EmailService;
import com.funding.funding.global.exception.ApiException;
import com.funding.funding.global.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
@Transactional(readOnly = true) // 기본은 조회 전용, 수정 메서드에만 개별 @Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwt;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    // 비밀번호 재설정 링크에 사용할 프론트 주소
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // RefreshToken Redis 저장 기간(일)
    @Value("${jwt.refresh-token-exp-days:7}")
    private long refreshTokenExpDays;

    public AuthService(UserRepository userRepository,
                       EmailVerificationTokenRepository emailTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwt,
                       EmailService emailService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.emailTokenRepository = emailTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    // 회원가입
    @Transactional
    public void register(AuthDtos.RegisterReq req) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(req.nickname())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        // LOCAL 회원 생성
        // 비밀번호는 반드시 암호화해서 저장
        User user = new User(
                req.email(),
                req.nickname(),
                passwordEncoder.encode(req.password()),
                UserRole.USER,
                UserStatus.ACTIVE,
                AuthProvider.LOCAL
        );

        userRepository.save(user);

        // 회원가입 직후 이메일 인증 코드 발송
        sendVerificationCode(req.email());
    }

    // 일반 로그인
    @Transactional
    public AuthDtos.TokenRes login(AuthDtos.LoginReq req) {

        // 이메일로 회원 조회
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED,
                                "이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 검증
        if (user.getPassword() == null ||
                !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 계정 상태 검증
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "탈퇴된 계정입니다.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "정지된 계정입니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "활성 상태의 계정이 아닙니다.");
        }

        // 마지막 로그인 시각 갱신
        user.updateLastLoginAt();

        // AccessToken / RefreshToken 발급
        String accessToken = jwt.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwt.createRefreshToken(user.getId());

        // RefreshToken은 Redis에 저장
        refreshTokenService.save(user.getId(), refreshToken, refreshTokenExpDays);

        return new AuthDtos.TokenRes(accessToken, refreshToken);
    }

    // 소셜 로그인
    @Transactional
    public AuthDtos.TokenRes socialLogin(
            AuthProvider provider,
            String providerId,
            String email,
            String nickname,
            String profileImage
    ) {
        // provider가 내려준 고유 ID는 필수
        if (providerId == null || providerId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "소셜 계정 ID를 가져올 수 없습니다.");
        }

        // 현재 정책상 이메일도 필수
        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "소셜 계정에서 이메일 정보를 제공하지 않았습니다.");
        }

        // provider + providerId 기준으로 기존 회원 조회
        // 없으면 자동 회원가입
        User user = userRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> createSocialUser(provider, providerId, email, nickname, profileImage));

        // 계정 상태 검증
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "탈퇴된 계정입니다.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "정지된 계정입니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "활성 상태의 계정이 아닙니다.");
        }

        // 최신 소셜 프로필 정보 반영
        user.updateSocialInfo(nickname, profileImage, providerId);

        // 토큰 발급
        String accessToken = jwt.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwt.createRefreshToken(user.getId());

        // RefreshToken Redis 저장
        refreshTokenService.save(user.getId(), refreshToken, refreshTokenExpDays);

        return new AuthDtos.TokenRes(accessToken, refreshToken);
    }

    // 소셜 회원 자동 생성
    @Transactional
    protected User createSocialUser(
            AuthProvider provider,
            String providerId,
            String email,
            String nickname,
            String profileImage
    ) {
        // 이미 같은 이메일의 회원이 존재하면 현재 정책상 충돌 처리
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (existingUser.getProvider() == AuthProvider.LOCAL) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "이미 일반회원으로 가입된 이메일입니다. 기존 이메일 로그인으로 로그인해주세요."
                );
            }

            if (existingUser.getProvider() == provider) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "이미 같은 소셜 계정 이메일로 가입된 회원입니다. 기존 방식으로 로그인해주세요."
                );
            }

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "이미 다른 소셜 계정으로 가입된 이메일입니다. 기존 방식으로 로그인해주세요."
            );
        });

        // 닉네임 중복 방지
        String finalNickname = makeUniqueNickname(nickname, provider);

        User newUser = User.createSocialUser(
                email,
                finalNickname,
                provider,
                providerId,
                profileImage
        );

        return userRepository.save(newUser);
    }

    // RefreshToken으로 AccessToken 재발급
    public AuthDtos.TokenRes refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "RefreshToken이 필요합니다.");
        }

        // JWT 서명 / 만료 검증
        try {
            if (!jwt.validate(refreshToken)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "유효하지 않은 RefreshToken입니다.");
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 RefreshToken입니다.");
        }

        // refresh token인지 타입 확인
        if (!"refresh".equals(jwt.getType(refreshToken))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "RefreshToken이 아닙니다.");
        }

        // 토큰 subject에서 userId 추출
        Long userId = jwt.getUserId(refreshToken);

        // Redis에 저장된 RefreshToken과 비교
        String savedRefreshToken = refreshTokenService.get(userId);

        if (savedRefreshToken == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그아웃되었거나 만료된 RefreshToken입니다.");
        }

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "RefreshToken이 일치하지 않습니다.");
        }

        // 사용자 상태 재검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "탈퇴된 계정입니다.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "정지된 계정입니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "활성 상태의 계정이 아닙니다.");
        }

        // AccessToken만 새로 발급, RefreshToken은 그대로 유지
        String newAccessToken = jwt.createAccessToken(user.getId(), user.getRole().name());

        return new AuthDtos.TokenRes(newAccessToken, refreshToken);
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        // Redis에 저장된 RefreshToken 삭제
        refreshTokenService.delete(userId);
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(Long userId, AuthDtos.WithdrawReq req) {
        // 로그인 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 이미 탈퇴한 계정이면 중복 탈퇴 방지
        if (user.getStatus() == UserStatus.DELETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 탈퇴한 계정입니다.");
        }

        // LOCAL 계정만 현재 비밀번호 검증
        // 소셜 회원(KAKAO / NAVER)은 비밀번호가 없으므로 검증하지 않음
        if (user.getProvider() == AuthProvider.LOCAL) {
            if (req.currentPassword() == null || req.currentPassword().isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호를 입력해주세요.");
            }

            if (user.getPassword() == null ||
                    !passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
            }
        }

        // 상태를 DELETED로 변경 (소프트 삭제)
        user.withdraw(req.reason());

        // 탈퇴 후 재발급 방지를 위해 RefreshToken 제거
        refreshTokenService.delete(user.getId());
    }

    // 이메일 인증 코드 발송
    @Transactional
    public void sendVerificationCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다.");
        }

        // 6자리 숫자 인증코드 생성
        String code = generateSixDigitCode();

        // 5분 유효한 인증 토큰 생성
        EmailVerificationToken token = EmailVerificationToken.create(email, code, 5);

        emailTokenRepository.save(token);
        emailService.sendVerificationCode(email, code);
    }

    // 이메일 인증 코드 확인
    @Transactional
    public void verifyEmail(AuthDtos.VerifyEmailReq req) {
        // 가장 최근 인증 코드 조회
        EmailVerificationToken token = emailTokenRepository
                .findTopByEmailOrderByCreatedAtDesc(req.email())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "인증 코드를 먼저 요청해주세요."));

        if (token.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 사용된 인증 코드입니다.");
        }

        if (token.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }

        if (!token.getCode().equals(req.code())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "인증 코드가 올바르지 않습니다.");
        }

        // 인증 성공 처리
        token.markUsed();
        userRepository.findByEmail(req.email()).ifPresent(User::verifyEmail);
    }

    // 닉네임으로 이메일(아이디) 찾기
    public AuthDtos.FindEmailRes findEmail(AuthDtos.FindEmailReq req) {
        User user = userRepository.findByNickname(req.nickname())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "해당 닉네임의 회원을 찾을 수 없습니다."));

        // 마스킹된 이메일 생성
        String masked = maskEmail(user.getEmail());

        // 필요 시 실제 메일로도 안내 가능
        emailService.sendFoundEmail(user.getEmail(), masked);

        return new AuthDtos.FindEmailRes(masked);
    }

    // 비밀번호 재설정 링크 발송
    @Transactional
    public void requestPasswordReset(AuthDtos.PasswordResetRequestReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다."));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.create(user, token, 30);

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetLink(user.getEmail(), token, frontendUrl);
    }

    // 비밀번호 재설정 실행
    @Transactional
    public void resetPassword(AuthDtos.PasswordResetReq req) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(req.token())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.NOT_FOUND, "유효하지 않은 토큰입니다."));

        if (resetToken.isUsed()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 사용된 토큰입니다.");
        }

        if (resetToken.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "만료된 토큰입니다. 비밀번호 찾기를 다시 시도해주세요.");
        }

        // 토큰 사용 처리 후 비밀번호 변경
        resetToken.markUsed();
        resetToken.getUser().changePassword(passwordEncoder.encode(req.newPassword()));
    }

    // 6자리 인증 코드 생성
    private String generateSixDigitCode() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    // 이메일 마스킹: test@example.com -> te**@example.com
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');

        if (atIndex <= 2) return email;

        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String masked = local.substring(0, 2) + "*".repeat(local.length() - 2);

        return masked + domain;
    }

    // 닉네임 중복 시 뒤에 숫자를 붙여 고유하게 만듦
    private String makeUniqueNickname(String nickname, AuthProvider provider) {
        String base = (nickname == null || nickname.isBlank())
                ? provider.name().toLowerCase() + "_user"
                : nickname.trim();

        String candidate = base;
        int suffix = 1;

        while (userRepository.existsByNickname(candidate)) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }
}