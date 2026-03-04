package com.funding.funding.domain.project.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.project.status.ProjectStatus;

import jakarta.transaction.Transactional;

@Service
public class ProjectFundingTransitionService {

    private final ProjectRepository projectRepository;

    public ProjectFundingTransitionService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * APPROVED 상태이고 start_at <= 현재시간 인 프로젝트를 FUNDING으로 전환
     */
    @Transactional
    public int transitionApprovedToFunding(LocalDateTime now) {

        List<Project> targets =
                projectRepository.findByStatusAndStartAtLessThanEqual(
                        ProjectStatus.APPROVED,
                        now
                );

        for (Project project : targets) {
            project.startFunding();
        }

        return targets.size();
    }
}

// 핵심 로직은 Service에 두고 Scheduler는 이 역할만 수행 주기적으로 Service 호출 이렇게 설계