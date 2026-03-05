package com.funding.funding.domain.donation.repository;

import com.funding.funding.domain.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long>{

    // 프로젝트 ID 기준으로 후원 목록을 생성일 내림차순 조회
    List<Donation> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    // 사용자 ID 기준으로 후원 목록을 생성일 내림차순 조회
    List<Donation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
