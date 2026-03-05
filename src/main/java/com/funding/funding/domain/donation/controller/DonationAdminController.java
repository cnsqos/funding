package com.funding.funding.domain.donation.controller;

import com.funding.funding.domain.donation.dto.AdminDonationResponse;
import com.funding.funding.domain.donation.service.query.DonationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스는 REST API 컨트롤러다 (JSON 반환)
@RequiredArgsConstructor // final 필드를 생성자로 자동 주입
@RequestMapping("/admin/donations") // 기본 URL 경로

// [권한] ADMIN 전용 API
// - 전체 후원 목록 조회
// - 운영/정산 용도

// 관리자 전체 후원 목록 조회 컨트롤러
public class DonationAdminController {

    private final DonationQueryService donationQueryService; // 鈺곌퀬???袁⑹뒠 ??뺥돩??

    @GetMapping // GET /admin/donations
    public Page<AdminDonationResponse> findAll(
            @PageableDefault(size = 20) Pageable pageable // ??륁뵠筌왖 ?類ｋ궖 ?癒?짗 雅뚯눘??
    ) {
        // 서비스에 페이지 정보 넘겨서 전체 후원 목록 조회
        return donationQueryService.findAllDonations(pageable);
    }
}