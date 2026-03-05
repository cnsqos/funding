package com.funding.funding.domain.donation.service.cancel;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.status.DonationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/*
 * [역할]
 * 사용자 취소(개인 환불) 전용 서비스
 * - 실제 돈이 오가는것이 아니라서 취소 상태만 가진다.
 * - 결제 완료 상태를 되돌리는 도메인 상태 변경 로직이다.
 *
 * [책임]
 * SUCCESS → CANCEL 상태 전이
 * 24시간 이내만 허용
 */
@Service
@RequiredArgsConstructor
public class DonationCancelService {

    private final DonationRepository donationRepository;

    @Transactional
    public void cancel(Long donationId) {

        // 1. 후원 조회
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // 2. 상태 전이 가능 여부 확인 (SUCCESS → CANCEL)
        if (!donation.getStatus().canTransitionTo(DonationStatus.CANCEL)) {
            throw new IllegalStateException("Invalid state transition");
        }

        // 3. 취소 가능 시간 검증
        if (LocalDateTime.now().isAfter(donation.getCancelDeadline())) {
            throw new IllegalStateException("Cancel deadline exceeded");
        }

        // 4. 상태 변경
        donation.setStatus(DonationStatus.CANCEL);
    }
}