package com.funding.funding.domain.project.facade;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.project.status.ProjectStatus;

import jakarta.transaction.Transactional;

@Component
public class ProjectFundingCompletionFacade {

    private final ProjectRepository projectRepository;
    private final DonationAggregationPort donationAggregationPort;

    public ProjectFundingCompletionFacade(ProjectRepository projectRepository,
                                         DonationAggregationPort donationAggregationPort) {
        this.projectRepository = projectRepository;
        this.donationAggregationPort = donationAggregationPort;
    }

    @Transactional
    public int completeExpiredFunding(LocalDateTime now) {
        List<Project> targets =
                projectRepository.findByStatusAndDeadlineLessThanEqual(ProjectStatus.FUNDING, now);

        for (Project project : targets) {
            long total = donationAggregationPort.sumSuccessAmountByProjectId(project.getId());

            // ✅ goal_amount(BIGINT) -> Long goalAmount 라고 가정
            long goal = project.getGoalAmount() == null ? 0L : project.getGoalAmount();
            boolean success = total >= goal;

            project.completeFunding(success); // ✅ 상태 전이 + 로그
        }

        return targets.size();
    }
}