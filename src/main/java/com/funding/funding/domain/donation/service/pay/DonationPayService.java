package com.funding.funding.domain.donation.service.pay;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.status.DonationStatus;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/*
 * [역할]
 * 후원 결제 흐름 담당 서비스
 *
 * [책임]
 * 1. 금액 정책 검증
 * 2. PENDING 상태 생성
 * 3. 결제 성공 시 SUCCESS 전환
 *
 * [병합 수정 내역]
 * Donation 엔티티가 @ManyToOne 방식으로 변경됨에 따라
 * setUserId() → User 객체 조회 후 setUser()
 * setProjectId() → Project 객체 조회 후 setProject() 로 수정
 */
@Service
@RequiredArgsConstructor
public class DonationPayService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;         // User 객체 조회용
    private final ProjectRepository projectRepository;   // Project 객체 조회용

    @Transactional
    public Donation createDonation(Long userId, Long projectId, Long amount) {

        // 1. 금액 검증 (최소 1000원 + 1000원 단위)
        if (amount < 1000 || amount % 1000 != 0) {
            throw new IllegalArgumentException("Invalid donation amount");
        }

        // 2. User, Project 객체 조회 (@ManyToOne 방식이므로 객체 자체를 넣어야 함)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 현재 시각을 기준으로 취소 마감 시간 계산
        LocalDateTime now = LocalDateTime.now();

        Donation donation = new Donation();

        // 어떤 사용자가 후원하는지 (User 객체로 세팅)
        donation.setUser(user);

        // 어떤 프로젝트에 후원하는지 (Project 객체로 세팅)
        donation.setProject(project);

        // 얼마를 후원하는지
        donation.setAmount(amount);

        // 아직 결제 전 상태 → PENDING
        donation.setStatus(DonationStatus.PENDING);

        // 생성 시점 기준 24시간 동안 취소 가능
        donation.setCancelDeadline(now.plusHours(24));

        // DB에 저장
        return donationRepository.save(donation);
    }

    @Transactional
    public void markSuccess(Long donationId) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // 현재 상태가 SUCCESS로 전이 가능한지 검증
        if (!donation.getStatus().canTransitionTo(DonationStatus.SUCCESS)) {
            throw new IllegalStateException("Invalid state transition");
        }

        donation.setStatus(DonationStatus.SUCCESS);

        // ✅ 프로젝트 currentAmount 업데이트
        donation.getProject().increaseCurrentAmount(donation.getAmount());
    }

    @Transactional
    public void markFailed(Long donationId) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Donation not found"));

        // PENDING → FAILED만 허용
        if (!donation.getStatus().canTransitionTo(DonationStatus.FAILED)) {
            throw new IllegalStateException("Invalid state transition");
        }

        donation.setStatus(DonationStatus.FAILED);
    }
}