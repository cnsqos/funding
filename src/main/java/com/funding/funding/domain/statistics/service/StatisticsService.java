package com.funding.funding.domain.statistics.service;

import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.statistics.dto.StatsResponse;
import com.funding.funding.domain.user.entity.UserStatus;
import com.funding.funding.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final DonationRepository donationRepository;

    public StatisticsService(UserRepository userRepository,
                             ProjectRepository projectRepository,
                             DonationRepository donationRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.donationRepository = donationRepository;
    }

    // 실시간 집계 통계
    public StatsResponse getStats() {
        StatsResponse r = new StatsResponse();

        // 회원 통계
        r.totalUsers = userRepository.count();
        r.activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        r.suspendedUsers = userRepository.countByStatus(UserStatus.SUSPENDED);

        // 프로젝트 통계
        r.totalProjects = projectRepository.count();
        r.fundingProjects = projectRepository.countByStatus(ProjectStatus.FUNDING);
        r.successProjects = projectRepository.countByStatus(ProjectStatus.SUCCESS);
        r.failedProjects = projectRepository.countByStatus(ProjectStatus.FAILED);

        // 후원 통계
        r.totalDonations = donationRepository.count();
        Long amount = donationRepository.sumSuccessAmount();
        r.successDonationAmount = amount != null ? amount : 0L;

        return r;
    }
}