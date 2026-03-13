package com.funding.funding.domain.project.service.query;

import com.funding.funding.domain.project.dto.ProjectSummaryResponse;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.repository.LikeRepository;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.global.exception.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 실제 DB 조회 로직 담당(Repository 호출 담당)
// 컨트롤러는 요청/응답만 하도록 얇게 유지

@Service
public class ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final LikeRepository likeRepository;
    private final ProjectViewService projectViewService;

    public ProjectQueryService(ProjectRepository projectRepository,
                               LikeRepository likeRepository,
                               ProjectViewService projectViewService) {
        this.projectRepository  = projectRepository;
        this.likeRepository     = likeRepository;
        this.projectViewService = projectViewService;
    }

    // 단건 프로젝트 조회
    // readOnly = true → 조회 전용 트랜잭션 (데이터 수정 없음)
    // ✅ DELETED 프로젝트는 404 반환 — 삭제된 프로젝트에 직접 접근 불가
    @Transactional(readOnly = true)
    public Project getOne(Long id) {
        Project project = projectRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다. id=" + id));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "삭제된 프로젝트입니다. id=" + id);
        }

        return project;
    }

    @Transactional
    public Project getOneWithViewCount(Long id) {
        Project project = projectRepository.findDetailById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다. id=" + id));

        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "삭제된 프로젝트입니다. id=" + id);
        }

        projectViewService.increaseView(project);

        return project;
    }

    // 좋아요 수 조회 (단건)
    @Transactional(readOnly = true)
    public long getLikeCount(Long projectId) {
        return likeRepository.countByIdProjectId(projectId);
    }

    /*
      프로젝트 목록 검색 — Page<ProjectSummaryResponse> 반환
      - status, categoryId, keyword, tagName 조건으로 필터링 가능
      - sortBy == "likes" 이면 좋아요 수 내림차순 정렬
      - 그 외에는 Pageable 기반 정렬 (기본: 최신순)
      - ✅ DELETED 상태 프로젝트는 Repository JPQL에서 자동 제외됨

      ✅ 변경사항:
        - 반환 타입을 Page<Project> → Page<ProjectSummaryResponse> 로 변경
          이유: LazyInitializationException 방지
               Page.map() 이 @Transactional 밖(컨트롤러)에서 실행되면
               Hibernate 세션이 이미 닫혀 category/owner Lazy 로딩 실패
               → 매핑을 트랜잭션 안에서 수행해서 세션이 열린 상태로 접근
        - tagName 파라미터 : 특정 태그로 필터링 (예: tagName=서울)
        - sortBy 파라미터  : "likes" 입력 시 인기순 정렬
        - keyword 검색 범위 확장: 제목 + 태그명 동시 검색 (Repository JPQL에서 처리)
     */
    @Transactional(readOnly = true)
    public Page<ProjectSummaryResponse> search(
            ProjectStatus status,
            Long categoryId,
            String keyword,
            String tagName,
            String sortBy,
            Pageable pageable
    ) {
        String kw  = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String tag = (tagName != null && !tagName.isBlank()) ? tagName.trim() : null;

        Page<Project> projects;

        if ("likes".equalsIgnoreCase(sortBy)) {
            Pageable noSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            projects = projectRepository.searchOrderByLikes(status, categoryId, kw, tag, noSort);
        } else {
            projects = projectRepository.search(status, categoryId, kw, tag, pageable);
        }

        // ✅ 트랜잭션이 열린 상태에서 매핑 — category/owner Lazy 로딩 안전
        return projects.map(p -> ProjectSummaryResponse.from(p, likeRepository.countByIdProjectId(p.getId())));
    }
}