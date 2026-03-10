package com.funding.funding.domain.user.controller;

import com.funding.funding.domain.user.dto.SuspendRequest;
import com.funding.funding.domain.user.dto.UserProfileResponse;
import com.funding.funding.domain.user.service.user.UserService;
import com.funding.funding.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/admin/users — 회원 전체 목록
    @GetMapping
    public ApiResponse<List<UserProfileResponse>> getAllUsers() {
        List<UserProfileResponse> result = userService.getAllUsers()
                .stream()
                .map(UserProfileResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // PATCH /api/admin/users/{userId}/suspend — 회원 정지
    @PatchMapping("/{userId}/suspend")
    public ApiResponse<Void> suspend(
            @PathVariable Long userId,
            @Valid @RequestBody SuspendRequest req
    ) {
        userService.suspend(userId, req.reason);
        return ApiResponse.ok(null);
    }

    // PATCH /api/admin/users/{userId}/activate — 정지 해제
    @PatchMapping("/{userId}/activate")
    public ApiResponse<Void> activate(@PathVariable Long userId) {
        userService.activate(userId);
        return ApiResponse.ok(null);
    }
}