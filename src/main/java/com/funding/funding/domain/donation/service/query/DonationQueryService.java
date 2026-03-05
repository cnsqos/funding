package com.funding.funding.domain.donation.service.query;

import com.funding.funding.domain.donation.dto.ProjectDonationResponse;
import com.funding.funding.domain.donation.dto.UserDonationResponse;
import com.funding.funding.domain.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.funding.funding.domain.donation.dto.AdminDonationResponse;

import java.util.List;

/*
 * 후원 조회 전용 서비스.
 *
 * - 상태 변경 로직은 포함하지 않는다.
 * - readOnly 트랜잭션 사용 (조회 전용).
 * - 권한 검증은 Controller 또는 상위 계층에서 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationQueryService {

    // 후원 데이터 접근 레이어
    private final DonationRepository donationRepository;

    /*
     * 특정 프로젝트의 후원 목록 조회.
     *
     * - projectId 기준으로 최신순 정렬 조회.
     * - 엔티티를 그대로 반환하지 않고 DTO로 변환한다.
     * - 프로젝트 작성자에게 필요한 정보만 노출한다
     *   (금액, 생성일, 상태).
     */

    // 프로젝트 후원 목록을 최신순으로 조회하여 DTO로 반환
    public List<ProjectDonationResponse> findProjectDonations(Long projectId) {

        return donationRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                // 엔티티 → DTO 변환 (정보 노출 범위 제한)
                .map(d -> new ProjectDonationResponse(
                        d.getAmount(),
                        d.getCreatedAt(),
                        d.getStatus()
                ))
                .toList();
    }
    // 사용자 후원 목록을 최신순으로 조회하여 DTO로 반환
    public List<UserDonationResponse> findUserDonations(Long userId) {

        return donationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                // 엔티티 → DTO 변환
                .map(d -> new UserDonationResponse(
                        d.getProjectId(),
                        d.getAmount(),
                        d.getStatus(),
                        d.getCreatedAt()
                ))
                .toList();
    }

    // 전체 후원 목록을 페이징 조회하여 관리자용 DTO로 반환
    public Page<AdminDonationResponse> findAllDonations(Pageable pageable) {

        return donationRepository.findAll(pageable)
                // 엔티티 → DTO 변환
                .map(d -> new AdminDonationResponse(
                        d.getId(),
                        d.getUserId(),
                        d.getProjectId(),
                        d.getAmount(),
                        d.getStatus(),
                        d.getCreatedAt(),
                        d.getRefundedAt()
                ));
    }


}