package com.funding.funding.domain.project.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.funding.funding.domain.project.exception.InvalidProjectStatusTransitionException;
import com.funding.funding.domain.project.status.ProjectStatus;
import com.funding.funding.domain.project.status.ProjectStatusPolicy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * [역할]
 * - 프로젝트 정보를 저장하는 엔티티(현재는 최소 구현)
 *
 * [현재 목표]
 * - 상태 전이 규칙은 ProjectStatusPolicy로 검증하고
 * - 상태 변경이 성공하면 ProjectStatusLog를 1개 남긴다
 * - APPROVED 상태에서 start_at 도달 시 FUNDING으로 전환될 수 있도록 startAt(예약 시작일)을 가진다
 */
@Entity
public class Project {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.DRAFT;

    // ✅ 펀딩 시작 예약일 (DB: start_at)
    @Column(name = "start_at")
    private LocalDateTime startAt;
    
    // goal_Amount 필드 매핑
    @Column(name = "goal_amount")
    private Long goalAmount;

    // ✅ 상태 변경 로그 목록 (상태가 바뀔 때마다 1개씩 쌓임)
    private final List<ProjectStatusLog> statusLogs = new ArrayList<>();

    // ✅ 공통 상태 변경 메서드 (상태 전이의 유일한 관문)
    public void changeStatus(ProjectStatus nextStatus) {
        ProjectStatus from = this.status;

        if (!ProjectStatusPolicy.isAllowed(this.status, nextStatus)) {
            throw new InvalidProjectStatusTransitionException(this.status, nextStatus);
        }

        this.status = nextStatus;

        // ✅ 상태 변경 성공 후 로그 기록
        statusLogs.add(new ProjectStatusLog(
                null,               // projectId (지금 단계에서는 null 허용)
                from,               // beforeStatus
                nextStatus,         // afterStatus
                "USER",             // changedBy (임시)
                0L,                 // changedById (임시)
                LocalDateTime.now() // createdAt
        ));
    }

    // ✅ 심사 요청 행위 메서드
    public void requestReview() {
        changeStatus(ProjectStatus.REVIEW_REQUESTED);
    }

    // ✅ 펀딩 시작 예약일 설정 (승인 이후 “예약 시작”을 위해 사용)
    public void scheduleStart(LocalDateTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("startAt cannot be null");
        }
        this.startAt = startAt;
    }

    // ✅ 예약 시작일 도달 시 펀딩 시작 전환
    public void startFunding() {
        changeStatus(ProjectStatus.FUNDING);
    }
    
    public void completeFunding(boolean success) { // 종료 처리 메서드 추가
        changeStatus(success ? ProjectStatus.SUCCESS : ProjectStatus.FAILED);
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public List<ProjectStatusLog> getStatusLogs() {
        return statusLogs;
    }
    
    public Long getGoalAmount() { // getter 추가
        return goalAmount;
    }
    
    public Long getId() {
        return id;
    }
    
    // ✅ 프로젝트 수정 가능 여부 검증
    public void validateEditable() {
        if (this.status != ProjectStatus.DRAFT) {
            throw new IllegalStateException("프로젝트는 초안 상태에서만 수정할 수 있습니다.");
        }
    }
}