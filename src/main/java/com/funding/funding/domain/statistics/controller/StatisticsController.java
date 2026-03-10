package com.funding.funding.domain.statistics.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.statistics.dto.StatsResponse;
import com.funding.funding.domain.statistics.service.StatisticsService;
import com.funding.funding.global.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자 - 통계", description = "사이트 통계 대시보드")
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // GET /api/admin/stats — 실시간 사이트 통계
    @GetMapping
    public ApiResponse<StatsResponse> getStats() {
        return ApiResponse.ok(statisticsService.getStats());
    }
}