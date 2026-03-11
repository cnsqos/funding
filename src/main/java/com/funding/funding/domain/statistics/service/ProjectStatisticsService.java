package com.funding.funding.domain.statistics.service;

import com.funding.funding.domain.project.repository.ProjectDailyViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProjectStatisticsService {

    private final ProjectDailyViewRepository projectDailyViewRepository;

    public ProjectStatisticsService(ProjectDailyViewRepository projectDailyViewRepository) {
        this.projectDailyViewRepository = projectDailyViewRepository;
    }

    @Transactional(readOnly = true) // 주간 조회수 계산
    public long getWeeklyViewCount(Long projectId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDate endDate = today;

        return projectDailyViewRepository.sumViewCountBetween(projectId, startDate, endDate);
    }

    @Transactional(readOnly = true) // 월간 조회수 계산
    public long getMonthlyViewCount(Long projectId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today;

        return projectDailyViewRepository.sumViewCountBetween(projectId, startDate, endDate);
    }
}