package com.funding.funding.domain.statistics.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.statistics.dto.StatsResponse;
import com.funding.funding.domain.statistics.service.ProjectStatisticsService;
import com.funding.funding.domain.statistics.service.StatisticsService;
import com.funding.funding.global.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 통계", description = "사이트 통계 대시보드")
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')") // swagger 테스트에서 관리자 권한 없음으로 떠서 잠깐 주석처리
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ProjectStatisticsService projectStatisticsService;

    public StatisticsController(StatisticsService statisticsService,
    							ProjectStatisticsService projectStatisticsService) {
        this.statisticsService = statisticsService;
        this.projectStatisticsService = projectStatisticsService;
    }

    // GET /api/admin/stats — 실시간 사이트 통계
    @GetMapping
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.ok(statisticsService.getStats());
    }
    
    @GetMapping("/projects/{projectId}/views/weekly")
    public long getWeeklyViewCount(@PathVariable("projectId") Long projectId) {
        return projectStatisticsService.getWeeklyViewCount(projectId);
    }

    @GetMapping("/projects/{projectId}/views/monthly")
    public long getMonthlyViewCount(@PathVariable("projectId") Long projectId) {
        return projectStatisticsService.getMonthlyViewCount(projectId);
    }
}