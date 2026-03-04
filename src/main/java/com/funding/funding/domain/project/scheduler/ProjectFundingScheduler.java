package com.funding.funding.domain.project.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.funding.funding.domain.project.service.ProjectFundingTransitionService;

@Component
public class ProjectFundingScheduler {

    private final ProjectFundingTransitionService service;

    public ProjectFundingScheduler(ProjectFundingTransitionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 60_000) // 1분마다
    public void run() {
        service.transitionApprovedToFunding(LocalDateTime.now());
    }
}