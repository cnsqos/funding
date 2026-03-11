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
      GET /api/projects?keyword=카페            → 제목 또는 태그에 "카페" 포함
      GET /api/projects?tagName=서울            → "서울" 태그가 달린 프로젝트만
      GET /api/projects?sortBy=likes            → 좋아요 수 내림차순
      GET /api/projects?keyword=카페&sortBy=likes&page=0&size=10

      ✅ 변경사항:
        @param tagName  특정 태그 이름으로 필터 (선택, 정확히 일치)
        @param sortBy   정렬 기준 — "likes" 입력 시 인기순, 그 외 기본 정렬
        keyword 검색이 제목 + 태그명을 동시에 검색함 (서비스/레포지토리에서 처리)
     */
    @GetMapping
    public Page<ProjectSummaryResponse> search(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tagName,  // ✅ 태그 필터
            @RequestParam(required = false) String sortBy,   // ✅ 인기순 정렬 ("likes")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return queryService.search(status, categoryId, keyword, tagName, sortBy, pageable)
                .map(project -> ProjectSummaryResponse.from(project, queryService.getLikeCount(project.getId())));
    }

    /*
      프로젝트 단건 조회

      GET /api/projects/{id}

      ✅ DELETED 상태면 404 반환 (서비스에서 처리)
     */
    @GetMapping("/{id}")
    public ProjectDetailResponse getOne(@PathVariable Long id) {
        return ProjectDetailResponse.from(
                queryService.getOne(id),
                queryService.getLikeCount(id)
        );
    }
}