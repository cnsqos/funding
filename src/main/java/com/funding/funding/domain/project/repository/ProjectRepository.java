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

    // 조건을 만족하면 펀딩을 종료한다는 메서드
    // FUNDING 상태이면서 마감일이 지난 프로젝트 조회
    // - 스케줄러가 FUNDING → SUCCESS/FAILED 전환 시 사용
    List<Project> findByStatusAndDeadlineLessThanEqual(ProjectStatus status, LocalDateTime now);

    // 내 프로젝트 목록 (마이페이지)
    List<Project> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // 통계용 - 특정 상태 프로젝트 수
    long countByStatus(ProjectStatus status);

    // ─────────────────────────────────────────────────────────────────
    // 프로젝트 검색 (최신순 / 마감순 등 일반 정렬)
    //
    // ✅ 변경사항 3가지:
    //   1. DELETED 프로젝트 자동 제외 — 삭제된 프로젝트는 검색 결과에 나오지 않음
    //   2. keyword로 제목 + 태그 동시 검색
    //      - LOWER(p.title) LIKE ... : 제목에 keyword 포함 여부
    //      - EXISTS (ProjectTag 서브쿼리) : keyword를 가진 태그가 달린 프로젝트 여부
    //      - 서브쿼리 방식을 쓴 이유: LEFT JOIN + DISTINCT 방식은 Pageable과 함께
    //        COUNT 쿼리에서 오류가 자주 발생함. 서브쿼리가 더 안전함.
    //   3. tagName 파라미터 추가 — 정확히 일치하는 태그로만 필터링
    //      예) tagName=서울 → "서울" 태그가 달린 프로젝트만 조회
    // ─────────────────────────────────────────────────────────────────
    @Query("""
            SELECT p FROM Project p
            WHERE p.status <> com.funding.funding.domain.project.entity.ProjectStatus.DELETED
              AND (:status     IS NULL OR p.status = :status)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:keyword    IS NULL
                   OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR EXISTS (
                       SELECT 1 FROM ProjectTag pt
                       WHERE pt.project = p
                         AND LOWER(pt.tag.normalizedName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   ))
              AND (:tagName    IS NULL
                   OR EXISTS (
                       SELECT 1 FROM ProjectTag pt
                       WHERE pt.project = p
                         AND LOWER(pt.tag.normalizedName) = LOWER(:tagName)
                   ))
            """)
    Page<Project> search(
            @Param("status")     ProjectStatus status,
            @Param("categoryId") Long categoryId,
            @Param("keyword")    String keyword,
            @Param("tagName")    String tagName,
            Pageable pageable
    );

    // ─────────────────────────────────────────────────────────────────
    // 프로젝트 검색 (인기순 정렬 — 좋아요 수 많은 순)
    //
    // ✅ 신규 추가
    //   like_count 컬럼이 Project에 없기 때문에
    //   서브쿼리로 좋아요 수를 실시간 계산해서 정렬
    //   ORDER BY (SELECT COUNT(l) ...) DESC
    //
    //   countQuery를 별도로 지정한 이유:
    //   Pageable의 COUNT 쿼리가 ORDER BY 서브쿼리와 충돌하는 것을 방지
    // ─────────────────────────────────────────────────────────────────
    @Query(
            value = """
                SELECT p FROM Project p
                WHERE p.status <> com.funding.funding.domain.project.entity.ProjectStatus.DELETED
                  AND (:status     IS NULL OR p.status = :status)
                  AND (:categoryId IS NULL OR p.category.id = :categoryId)
                  AND (:keyword    IS NULL
                       OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR EXISTS (
                           SELECT 1 FROM ProjectTag pt
                           WHERE pt.project = p
                             AND LOWER(pt.tag.normalizedName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       ))
                  AND (:tagName    IS NULL
                       OR EXISTS (
                           SELECT 1 FROM ProjectTag pt
                           WHERE pt.project = p
                             AND LOWER(pt.tag.normalizedName) = LOWER(:tagName)
                       ))
                ORDER BY (SELECT COUNT(l) FROM Like l WHERE l.id.projectId = p.id) DESC
                """,
            countQuery = """
                SELECT COUNT(p) FROM Project p
                WHERE p.status <> com.funding.funding.domain.project.entity.ProjectStatus.DELETED
                  AND (:status     IS NULL OR p.status = :status)
                  AND (:categoryId IS NULL OR p.category.id = :categoryId)
                  AND (:keyword    IS NULL
                       OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       OR EXISTS (
                           SELECT 1 FROM ProjectTag pt
                           WHERE pt.project = p
                             AND LOWER(pt.tag.normalizedName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                       ))
                  AND (:tagName    IS NULL
                       OR EXISTS (
                           SELECT 1 FROM ProjectTag pt
                           WHERE pt.project = p
                             AND LOWER(pt.tag.normalizedName) = LOWER(:tagName)
                       ))
                """
    )
    Page<Project> searchOrderByLikes(
            @Param("status")     ProjectStatus status,
            @Param("categoryId") Long categoryId,
            @Param("keyword")    String keyword,
            @Param("tagName")    String tagName,
            Pageable pageable
    );
}