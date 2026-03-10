package com.funding.funding.domain.donation.repository;

import com.funding.funding.domain.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    // 프로젝트 ID 기준으로 후원 목록을 생성일 내림차순 조회
    // @ManyToOne 방식: project.id 로 탐색
    List<Donation> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    // 사용자 ID 기준으로 후원 목록을 생성일 내림차순 조회
    // @ManyToOne 방식: user.id 로 탐색
    List<Donation> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 통계용
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.status = 'SUCCESS'")
    Long sumSuccessAmount();

    long countByStatus(com.funding.funding.domain.donation.status.DonationStatus status);

}