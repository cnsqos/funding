package com.funding.funding.domain.donation.service.pay;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.status.DonationStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * [역할]
 * 후원 결제 흐름 담당 서비스
 *
 * [책임]
 * 1. 금액 정책 검증
 * 2. PENDING 상태 생성
 * 3. 결제 성공 시 SUCCESS 전환
 */
@Service
@RequiredArgsConstructor
public class DonationPayService {

    // DB 접근용 레포지토리 (final → 생성자로 주입)
	private final DonationRepository donationRepository;

    @Transactional
    public Donation createDonation(Long userId, Long projectId, Long amount) {

        // 1. 금액 검증 (최소 1000원 + 1000원 단위)
        if (amount < 1000 || amount % 1000 != 0) {
            throw new IllegalArgumentException("Invalid donation amount");
        }

        // 현재 시각을 기준으로 취소 마감 시간 계산
        LocalDateTime now = LocalDateTime.now();

        Donation donation = new Donation();

        // 어떤 사용자가 후원하는지
        donation.setUserId(userId);

        // 어떤 프로젝트에 후원하는지
        donation.setProjectId(projectId);

        // 얼마를 후원하는지
        donation.setAmount(amount);

        // 아직 결제 전 상태 → PENDING
        donation.setStatus(DonationStatus.PENDING);

        // 생성 시점 기준 24시간 동안 취소 가능
        donation.setCancelDeadline(now.plusHours(24));

        // DB에 저장 (INSERT 실행)
        return donationRepository.save(donation);
    }

    @Transactional
    public void markSuccess(Long donationId) {

        // DB에서 해당 후원 조회
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // 현재 상태가 SUCCESS로 전이 가능한지 검증
        if (!donation.getStatus().canTransitionTo(DonationStatus.SUCCESS)) {
            throw new IllegalStateException("Invalid state transition");
        }

        // 상태를 SUCCESS로 변경
        donation.setStatus(DonationStatus.SUCCESS);
    }
    
    
    @Transactional
    public void markFailed(Long donationId) {

        // DB에서 해당 후원 조회
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // PENDING → FAILED만 허용
        if (!donation.getStatus().canTransitionTo(DonationStatus.FAILED)) {
            throw new IllegalStateException("Invalid state transition");
        }
        // 상태를 FAILED로 변경
        donation.setStatus(DonationStatus.FAILED);
    }
    
    
    
    
    
}