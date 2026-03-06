package com.funding.funding.domain.donation.controller;

import com.funding.funding.domain.donation.dto.ProjectDonationResponse;
import com.funding.funding.domain.donation.dto.UserDonationResponse;
import com.funding.funding.domain.donation.service.query.DonationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 후원 조회 컨트롤러
//
// [TODO] 인증 연동 후 아래 항목 수정 필요:
// - findMyDonations: @RequestParam userId → @AuthenticationPrincipal에서 추출 - 완료
// - 프로젝트 작성자 본인 또는 관리자만 findProjectDonations 접근 가능하도록 권한 추가

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects") // ✅ /api/ prefix 통일
public class DonationController {

    private final DonationQueryService donationQueryService;

    // 특정 프로젝트의 후원 목록 조회 (프로젝트 작성자/관리자 전용)
    @GetMapping("/{projectId}/donations")
    public List<ProjectDonationResponse> findProjectDonations(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return donationQueryService.findProjectDonations(projectId, userId);
    }

    // 내 후원 목록 조회
    // TODO: 인증 연동 후 @RequestParam userId 제거하고 Authentication에서 추출 - 완료
    @GetMapping("/users/me/donations")
    public List<UserDonationResponse> findMyDonations(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return donationQueryService.findUserDonations(userId);
    }
}