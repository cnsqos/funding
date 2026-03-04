package com.funding.funding.domain.project.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.status.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * ✅ APPROVED 상태이면서 start_at <= now 인 프로젝트 조회
     * - 스케줄러/배치가 "예약 시작일 도달" 프로젝트를 FUNDING으로 전환할 때 사용
     */
    List<Project> findByStatusAndStartAtLessThanEqual(ProjectStatus status, LocalDateTime now);
    List<Project> findByStatusAndDeadlineLessThanEqual(ProjectStatus status, LocalDateTime now);
    // 전제 : Project에 Deadline 필드가 @Column(name="deadline") LocalDateTime deadline;으로 매핑돼 있어야함
}

// Repository가 DB에서 대상 선정 -> Service가 대상들을 순서대로 전환 실행
// 역할 분리