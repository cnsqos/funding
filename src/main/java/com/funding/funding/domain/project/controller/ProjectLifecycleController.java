package com.funding.funding.domain.project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.project.service.lifecycle.ProjectLifecycleService;
import com.funding.funding.global.exception.ApiException;
import com.funding.funding.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "프로젝트 상태 관리", description = "심사 요청, 승인, 반려, 중단, 재개")
@RestController
@RequestMapping("/api/projects")
public class ProjectLifecycleController {

    private final ProjectLifecycleService lifecycleService;

    public ProjectLifecycleController(ProjectLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    // ── 회원 전용 ─────────────────────────────────────

    // 심사 요청: DRAFT/REJECTED → REVIEW_REQUESTED (본인 프로젝트만)
    @PostMapping("/{id}/review-request")
    public ApiResponse<Void> requestReview(@PathVariable("id") Long id, Authentication auth) {
        lifecycleService.requestReview(id, extractUserId(auth));
        return ApiResponse.ok(null);
    }

    // 삭제 요청: FUNDING → DELETE_REQUESTED (본인 프로젝트만)
    @PostMapping("/{id}/delete-request")
    public ApiResponse<Void> requestDelete(@PathVariable("id") Long id, Authentication auth) {
        lifecycleService.requestDelete(id, extractUserId(auth));
        return ApiResponse.ok(null);
    }

    // ── 관리자 전용 ───────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable("id") Long id, Authentication auth) {
        lifecycleService.approve(id, extractUserId(auth));
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable("id") Long id, Authentication auth) {
        lifecycleService.reject(id, extractUserId(auth));
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/stop")
    public ApiResponse<Void> stop(@PathVariable("id") Long id, Authentication auth) {
        lifecycleService.stop(id, extractUserId(auth));
        return ApiResponse.ok(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/resume")
    public ApiResponse<Void> resume(@PathVariable("id") Long id, Authentication auth) {
        lifecycleService.resume(id, extractUserId(auth));
        return ApiResponse.ok(null);
    }

    // ────────────────────────────────────────────────
    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long id) return id;
        if (principal instanceof String s) return Long.valueOf(s);
        throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
    }
}