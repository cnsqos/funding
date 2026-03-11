package com.funding.funding.domain.project.dto;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;

import java.time.LocalDateTime;

// 프로젝트 상세 조회 응답 DTO
// - API 응답 전용 객체
// - 엔티티(Project)를 직접 반환하지 않고 DTO로 변환해서 반환
// - 엔티티 구조 변경이 API에 직접 영향을 주는 것을 방지
public class ProjectDetailResponse {

    // 프로젝트 기본 정보
    public Long id;
    public String title;        // 프로젝트 제목
    public String content;      // 프로젝트 상세 내용

    // 연관 엔티티 정보
    public String categoryName; // 카테고리 이름
    public String ownerNickname;// 작성자 닉네임

    // 프로젝트 상태
    public ProjectStatus status;

    // 금액 관련 정보
    public Long goalAmount;     // 목표 금액
    public Long currentAmount;  // 현재 후원 금액
    public int progressPercent; // 목표 대비 달성률 (%)

    // ✅ 추가 — 좋아요 수
    //    Project 엔티티에 like_count 컬럼이 없어서
    //    서비스에서 LikeRepository로 조회한 값을 주입받음
    public long likeCount;

    // 시간 정보
    public LocalDateTime startAt;
    public LocalDateTime deadline;
    public LocalDateTime createdAt;

    // ✅ likeCount를 외부에서 주입받는 팩토리 메서드
    public static ProjectDetailResponse from(Project p, long likeCount) {

        ProjectDetailResponse r = new ProjectDetailResponse();

        // 기본 필드 매핑
        r.id = p.getId();
        r.title = p.getTitle();
        r.content = p.getContent();

        // 연관 엔티티 null 안전 처리
        // category가 null이면 NPE 방지
        r.categoryName = p.getCategory() != null ? p.getCategory().getName() : null;

        // owner가 null이면 NPE 방지
        r.ownerNickname = p.getOwner() != null ? p.getOwner().getNickname() : null;

        // 프로젝트 상태
        r.status = p.getStatus();

        // 금액 정보
        r.goalAmount = p.getGoalAmount();
        r.currentAmount = p.getCurrentAmount();

        // 목표 금액 대비 달성률 계산
        // goalAmount가 null이거나 0이면 0% 처리
        r.progressPercent = (p.getGoalAmount() != null && p.getGoalAmount() > 0)
                ? (int) (p.getCurrentAmount() * 100L / p.getGoalAmount())
                : 0;

        // 좋아요 수
        r.likeCount = likeCount;

        // 시간 정보
        r.startAt = p.getStartAt();
        r.deadline = p.getDeadline();
        r.createdAt = p.getCreatedAt();

        return r;
    }

    // 기존 from(Project) 호환용 — likeCount 0으로 처리
    public static ProjectDetailResponse from(Project p) {
        return from(p, 0L);
    }
}d