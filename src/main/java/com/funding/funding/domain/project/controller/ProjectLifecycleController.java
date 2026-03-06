package com.funding.funding.domain.project.controller;

import com.funding.funding.domain.project.service.lifecycle.ProjectLifecycleService;
import com.funding.funding.global.security.JwtTokenProvider;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// 프로젝트 상태 전이 API
// 이미 만들어진 프로젝트의 상태를 바꿈
// [TODO] 인증 연동 후 아래 항목 수정 필요:
// 1. userId / adminId → Authentication에서 추출
// 2. 관리자 전용 API(approve, reject, stop, resume)에 @PreAuthorize("hasRole('ADMIN')") 추가 완
// 3. 사용자 전용 API(requestReview, requestDelete)에 본인 소유 프로젝트 검증 추가

@RestController
@RequestMapping("/api/projects") // ✅ /api/ prefix 통일
public class ProjectLifecycleController {

    private final ProjectLifecycleService lifecycleService;
    private final JwtTokenProvider jwtTokenProvider;
    
    public ProjectLifecycleController(ProjectLifecycleService lifecycleService,
            						  JwtTokenProvider jwtTokenProvider) {
    	this.lifecycleService = lifecycleService;
    	this.jwtTokenProvider = jwtTokenProvider;
    }

    // 심사 요청: DRAFT/REJECTED → REVIEW_REQUESTED
    // TODO: 인증 연동 후 userId를 Authentication에서 추출
    @PostMapping("/{id}/review-request")
    public ResponseEntity<Void> requestReview(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long userId = extractUserId(authorizationHeader);
        lifecycleService.requestReview(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    // 심사 승인: REVIEW_REQUESTED → APPROVED (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long adminId = extractUserId(authorizationHeader);
        lifecycleService.approve(id, adminId);
        return ResponseEntity.noContent().build();
    }

    // 심사 반려: REVIEW_REQUESTED → REJECTED (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long adminId = extractUserId(authorizationHeader);
        lifecycleService.reject(id, adminId);
        return ResponseEntity.noContent().build();
    }

    // 강제 중단: FUNDING → STOPPED (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/stop")
    public ResponseEntity<Void> stop(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long adminId = extractUserId(authorizationHeader);
        lifecycleService.stop(id, adminId);
        return ResponseEntity.noContent().build();
    }

    // 재개: STOPPED → FUNDING (관리자 전용)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resume(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long adminId = extractUserId(authorizationHeader);
        lifecycleService.resume(id, adminId);
        return ResponseEntity.noContent().build();
    }

    // 삭제 요청: FUNDING → DELETE_REQUESTED
    @PostMapping("/{id}/delete-request")
    public ResponseEntity<Void> requestDelete(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long userId = extractUserId(authorizationHeader);
        lifecycleService.requestDelete(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        return jwtTokenProvider.getUserId(token);
    }
}