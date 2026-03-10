package com.funding.funding.domain.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.project.dto.ProjectSummaryResponse;
import com.funding.funding.domain.user.dto.UserProfileResponse;
import com.funding.funding.domain.user.dto.UserProfileUpdateRequest;
import com.funding.funding.domain.user.service.user.UserService;
import com.funding.funding.global.exception.ApiException;
import com.funding.funding.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "마이페이지", description = "내 정보 조회, 프로필 수정, 내 프로젝트, 찜 목록")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users/me — 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(Authentication auth) {
        Long userId = extractUserId(auth);
        return ApiResponse.ok(UserProfileResponse.from(userService.getMe(userId)));
    }

    // PUT /api/users/me — 프로필 수정 (닉네임, 프로필 이미지)
    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(
            Authentication auth,
            @Valid @RequestBody UserProfileUpdateRequest req
    ) {
        Long userId = extractUserId(auth);
        return ApiResponse.ok(UserProfileResponse.from(userService.updateProfile(userId, req)));
    }

    // GET /api/users/me/projects — 내 프로젝트 목록
    @GetMapping("/me/projects")
    public ApiResponse<List<ProjectSummaryResponse>> myProjects(Authentication auth) {
        Long userId = extractUserId(auth);
        List<ProjectSummaryResponse> result = userService.getMyProjects(userId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // GET /api/users/me/likes — 찜 목록
    @GetMapping("/me/likes")
    public ApiResponse<List<ProjectSummaryResponse>> myLikes(Authentication auth) {
        Long userId = extractUserId(auth);
        List<ProjectSummaryResponse> result = userService.getLikedProjects(userId)
                .stream()
                .map(ProjectSummaryResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // ────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long id) return id;
        if (principal instanceof String s) return Long.valueOf(s);
        throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다");
    }
}