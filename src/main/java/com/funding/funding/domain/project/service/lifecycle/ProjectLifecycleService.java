package com.funding.funding.domain.project.service.lifecycle;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectLifecycleService {

    private final ProjectRepository projectRepository;

    public ProjectLifecycleService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // ────────────────────────────────────────
    // 사용자 요청
    // ────────────────────────────────────────

    // 심사 요청: DRAFT / REJECTED → REVIEW_REQUESTED
    @Transactional
    public void requestReview(Long projectId, Long userId) {
        Project project = findProject(projectId);
        validateOwner(project, userId);  // ✅ 본인 소유 검증
        project.requestReview(userId);
    }

    // 삭제 요청: FUNDING → DELETE_REQUESTED
    @Transactional
    public void requestDelete(Long projectId, Long userId) {
        Project project = findProject(projectId);
        validateOwner(project, userId);  // ✅ 본인 소유 검증
        project.changeStatus(ProjectStatus.DELETE_REQUESTED, "USER", userId);
    }

    // ────────────────────────────────────────
    // 관리자 요청
    // ────────────────────────────────────────

    // 심사 승인: REVIEW_REQUESTED → APPROVED
    @Transactional
    public void approve(Long projectId, Long adminId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.changeStatus(ProjectStatus.APPROVED, "ADMIN", adminId);
    }

    // 심사 반려: REVIEW_REQUESTED → REJECTED
    @Transactional
    public void reject(Long projectId, Long adminId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.changeStatus(ProjectStatus.REJECTED, "ADMIN", adminId);
    }

    // 강제 중단: FUNDING → STOPPED
    @Transactional
    public void stop(Long projectId, Long adminId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.changeStatus(ProjectStatus.STOPPED, "ADMIN", adminId);
    }

    // 재개 승인: STOPPED → FUNDING
    @Transactional
    public void resume(Long projectId, Long adminId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.changeStatus(ProjectStatus.FUNDING, "ADMIN", adminId);
    }

    // 삭제 완료: DELETE_REQUESTED → DELETED (환불 완료 후 시스템 호출)
    @Transactional
    public void completeDelete(Long projectId) {
        Project project = findProject(projectId);
        project.changeStatus(ProjectStatus.DELETED, "SYSTEM", 0L);
    }

    // ────────────────────────────────────────
    // 배치 / 스케줄러
    // ────────────────────────────────────────

    @Transactional
    public void transitionApprovedToFunding(LocalDateTime now) {
        List<Project> targets =
                projectRepository.findByStatusAndStartAtLessThanEqual(ProjectStatus.APPROVED, now);
        for (Project project : targets) {
            project.startFunding();
        }
    }

    @Transactional
    public int completeFundingByDeadline(LocalDateTime now) {
        List<Project> targets =
                projectRepository.findByStatusAndDeadlineLessThanEqual(ProjectStatus.FUNDING, now);
        for (Project project : targets) {
            project.completeFunding();
        }
        return targets.size();
    }

    // ────────────────────────────────────────
    // 공통 헬퍼
    // ────────────────────────────────────────

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
    }

    // ✅ 본인 소유 프로젝트인지 검증
    private void validateOwner(Project project, Long userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "본인의 프로젝트만 요청할 수 있습니다.");
        }
    }
}