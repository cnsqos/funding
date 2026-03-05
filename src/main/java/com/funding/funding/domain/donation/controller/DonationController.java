package com.funding.funding.domain.donation.controller;

import com.funding.funding.domain.donation.dto.ProjectDonationResponse;
import com.funding.funding.domain.donation.dto.UserDonationResponse;
import com.funding.funding.domain.donation.service.query.DonationQueryService;
import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // REST API 컨트롤러
@RequiredArgsConstructor // 생성자 주입
@RequestMapping("/projects") // 기본 경로

// [권한]
// - 프로젝트 작성자 또는 관리자만 조회 가능
// - (추후 Security에서 검증 예정)

// 후원 컨트롤러
public class DonationController {

    // 조회 로직 위임
    private final DonationQueryService donationQueryService;

    @GetMapping("/{projectId}/donations")
    public List<ProjectDonationResponse> findProjectDonations(
            @PathVariable Long projectId  // URL에서 projectId 추출
    ) {
        // 특정 프로젝트에 대한 후원 목록 조회
        return donationQueryService.findProjectDonations(projectId);
    }

    /*
      [권한] 로그인 사용자 전용
      - 본인의 후원 내역 조회
      - Security에서 인증된 사용자 ID를 전달받는다.
     */

    // userId는 임시로 RequestParam 받는 코드
    @GetMapping("/users/me/donations")
    public List<UserDonationResponse> findMyDonations(
            @RequestParam Long userId
    ) {
        return donationQueryService.findUserDonations(userId);
    }

    // 보안 추가해야하는 코드
    /*
    @GetMapping("/me")
    public List<UserDonationResponse> findMyDonations(
            // 로그인된 사용자 정보를 주입받는 어노테이션
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 로그인한 사용자 ID 추출
        Long userId = userDetails.getId(); // JWT????쇰선??덈뮉 ?????ID

        // 본인 후원 목록 조회
        return donationQueryService.findUserDonations(userId);
    }
    */
}
