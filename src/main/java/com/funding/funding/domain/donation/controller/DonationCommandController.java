package com.funding.funding.domain.donation.controller;

import com.funding.funding.domain.donation.service.cancel.DonationCancelService;
import com.funding.funding.domain.donation.service.pay.DonationPayService;
import com.funding.funding.domain.donation.service.refund.DonationRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DonationCommandController {

    private final DonationPayService donationPayService;
    private final DonationCancelService donationCancelService;
    private final DonationRefundService donationRefundService;

    // 후원하기
    @PostMapping("/projects/{projectId}/donations")
    public void donate(
            @PathVariable Long projectId,
            @RequestParam Long userId,
            @RequestParam Long amount
    ) {
        donationPayService.createDonation(userId, projectId, amount);
    }

    // 후원 취소
    @PostMapping("/donations/{donationId}/cancel")
    public void cancelDonation(@PathVariable Long donationId) {
        donationCancelService.cancel(donationId);
    }

    // 관리자 환불
    @PostMapping("/admin/donations/{donationId}/refund")
    public void refundDonation(@PathVariable Long donationId) {
        donationRefundService.refund(donationId);
    }
}
