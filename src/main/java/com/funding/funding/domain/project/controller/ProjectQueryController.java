package com.funding.funding.domain.project.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.project.dto.ProjectDetailResponse;
import com.funding.funding.domain.project.dto.ProjectSummaryResponse;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.service.query.ProjectQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

// 프로젝트 조회 API
@Tag(name = "프로젝트 조회", description = "프로젝트 목록, 상세 조회")
@RestController
@RequestMapping("/api/projects")
public class ProjectQueryController {

    private final ProjectQueryService queryService;

    public ProjectQueryController(ProjectQueryService queryService) {
        this.queryService = queryService;
    }

    /*
      프로젝트 목록 검색 / 필터링 / 페이징

      GET /api/projects
      GET /api/projects?status=FUNDING
      GET /api/projects?categoryId=1
      GET /api/projects?keyword=카페
      GET /api/projects?status=FUNDING&categoryId=2&keyword=카페&page=0&size=10&sort=createdAt,desc

      @param status     프로젝트 상태 필터 (선택)
      @param categoryId 카테고리 ID 필터 (선택)
      @param keyword    제목 검색어 (선택, 대소문자 무시)
      @param pageable   기본: 최신순 10개 / ?page=0&size=10&sort=createdAt,desc 로 변경 가능
     */
    @GetMapping
    public Page<ProjectSummaryResponse> search(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return queryService.search(status, categoryId, keyword, pageable)
                .map(ProjectSummaryResponse::from);
    }

    /*
      프로젝트 단건 조회

      GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ProjectDetailResponse getOne(@PathVariable Long id) {
        return ProjectDetailResponse.from(queryService.getOne(id));
    }
}