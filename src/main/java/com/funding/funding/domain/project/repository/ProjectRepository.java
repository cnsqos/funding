package com.funding.funding.domain.project.repository;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 조건을 만족하면 펀딩을 시작한다는 메서드
    // APPROVED 상태이면서 시작일이 도달한 프로젝트 조회
    // - 스케줄러가 APPROVED → FUNDING 전환 시 사용
    List<Project> findByStatusAndStartAtLessThanEqual(ProjectStatus status, LocalDateTime now);

    // // 조건을 만족하면 펀딩을 종료한다는 메서드
    // FUNDING 상태이면서 마감일이 지난 프로젝트 조회
    // - 스케줄러가 FUNDING → SUCCESS/FAILED 전환 시 사용
    List<Project> findByStatusAndDeadlineLessThanEqual(ProjectStatus status, LocalDateTime now);

    // ─────────────────────────────────────────
    // 이 메서드는 프로젝트 목록을 조회하면서
    // status / category / keyword 조건을 선택적으로 적용하고
    // 페이징 처리까지 한다
    // ─────────────────────────────────────────
    /*
      조건 검색 (status, categoryId, keyword 모두 선택)

      사용 예:
        GET /api/projects?status=FUNDING&categoryId=2&keyword=카페&page=0&size=10&sort=createdAt,desc

      각 파라미터가 null이면 해당 조건을 무시한다.
      Spring Data JPA의 JPQL로 동적 필터를 한 쿼리로 처리한다.
     */

    @Query("""
            SELECT p FROM Project p
            WHERE (:status    IS NULL OR p.status = :status)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword    IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Project> search(
            @Param("status")     ProjectStatus status,
            @Param("categoryId") Long categoryId,
            @Param("keyword")    String keyword,
            Pageable pageable
    );

    // 내 프로젝트 목록 (마이페이지)
    List<Project> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // 통계용 - 특정 상태 프로젝트 수
    long countByStatus(ProjectStatus status);
}