package com.funding.funding.domain.project.service.query;

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

@Service // 비즈니스 로직을 담당
public class ProjectQueryService {

    private final ProjectRepository projectRepository; // DB 조회를 위한 Repository
    private final LikeRepository likeRepository;       // ✅ 추가 — 좋아요 수 조회용
    private final ProjectViewService projectViewService;

    public ProjectQueryService(ProjectRepository projectRepository,
                               LikeRepository likeRepository,
                               ProjectViewService projectViewService) { // 생성자 주입
        this.projectRepository = projectRepository;
        this.likeRepository    = likeRepository;
        this.projectViewService = projectViewService;
    }

    // 단건 프로젝트 조회
    // readOnly = true → 조회 전용 트랜잭션 (데이터 수정 없음)
    // ✅ DELETED 프로젝트는 404 반환 — 삭제된 프로젝트에 직접 접근 불가
    @Transactional(readOnly = true)
    public Project getOne(Long id) { // 특정 프로젝트 하나 조회, 요청이 들어오면 Controller가 이 메서드 호출
        // id로 프로젝트 조회
        // findById는 Optional 반환 → 값이 없으면 예외 발생
        Project project = projectRepository.findDetailById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다. id=" + id));

        // ✅ DELETED 상태면 존재하더라도 404 반환
        //    삭제 요청 후 완전히 삭제된 프로젝트는 외부에서 접근할 수 없어야 함
        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new ApiException(HttpStatus.NOT_FOUND, "삭제된 프로젝트입니다. id=" + id);
        }

        return project;
    }
    
    @Transactional // 조회수 메서드 추가
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
    // — getOne() 이후 서비스 레이어에서 호출
    @Transactional(readOnly = true)
    public long getLikeCount(Long projectId) {
        return likeRepository.countByIdProjectId(projectId);
    }

    /*
      프로젝트 목록 검색
      - status, categoryId, keyword, tagName 조건으로 필터링 가능
      - sortBy == "likes" 이면 좋아요 수 내림차순 정렬
      - 그 외에는 Pageable 기반 정렬 (기본: 최신순)
      - ✅ DELETED 상태 프로젝트는 Repository JPQL에서 자동 제외됨

      ✅ 변경사항:
        - tagName 파라미터 추가 : 특정 태그로 필터링 (예: tagName=서울)
        - sortBy 파라미터 추가  : "likes" 입력 시 인기순 정렬
        - keyword 검색 범위 확장: 제목 + 태그명 동시 검색 (Repository JPQL에서 처리)

      사용 예:
        GET /api/projects?keyword=카페           → 제목이나 태그에 "카페" 포함
        GET /api/projects?tagName=서울           → "서울" 태그가 달린 프로젝트
        GET /api/projects?sortBy=likes           → 좋아요 많은 순
        GET /api/projects?keyword=카페&sortBy=likes → "카페" 키워드 + 인기순
     */
    @Transactional(readOnly = true)
    public Page<Project> search(
            ProjectStatus status,
            Long categoryId,
            String keyword,
            String tagName,   // ✅ 태그 이름으로 필터링 (정확히 일치)
            String sortBy,    // ✅ "likes" 입력 시 인기순 정렬
            Pageable pageable
    ) {
        // keyword가 null이 아니고 빈 문자열이 아닐 때만 사용
        // "   카페   " 같은 입력은 trim()으로 공백 제거
        // 빈 문자열이면 null로 변환하여 전체 LIKE 검색 방지
        String kw  = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        // tagName도 동일하게 공백 처리
        String tag = (tagName != null && !tagName.isBlank()) ? tagName.trim() : null;

        // ✅ 인기순 정렬
        //    "likes" 를 요청한 경우 좋아요 수 COUNT 서브쿼리로 정렬하는 별도 쿼리 사용
        //    이때 Pageable의 sort는 무시하고 JPQL ORDER BY를 따름
        //    → PageRequest.of(page, size) 로 sort 없이 페이징만 적용
        if ("likes".equalsIgnoreCase(sortBy)) {
            Pageable noSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            return projectRepository.searchOrderByLikes(status, categoryId, kw, tag, noSort);
        }

        // Repository의 JPQL 검색 메서드 호출
        // 조건(status, categoryId, keyword, tagName) + 페이징(pageable)을 전달
        return projectRepository.search(status, categoryId, kw, tag, pageable);
    }
}