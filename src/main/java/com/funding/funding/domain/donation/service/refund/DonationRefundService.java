package com.funding.funding.domain.donation.service.refund;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.status.DonationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


/*
 * [역할]
 * 후원 환불 전용 서비스
 *
 * [책임]
 * SUCCESS → REFUND 전이
 * 환불 처리 시간 기록
 */
@Service
@RequiredArgsConstructor
public class DonationRefundService {

    private final DonationRepository donationRepository;

    @Transactional
    public void refund(Long donationId) {

        // 1. 후원 조회
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // 2. 상태 전이 가능 여부 검증
        if (!donation.getStatus().canTransitionTo(DonationStatus.REFUND)) {
            throw new IllegalStateException("Invalid state transition");
        }

        // 3. 상태 변경
        donation.setStatus(DonationStatus.REFUND);

        // 4. 환불 처리 시간 기록
        donation.setRefundedAt(LocalDateTime.now());
    }
}