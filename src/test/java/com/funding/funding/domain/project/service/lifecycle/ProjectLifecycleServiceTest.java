package com.funding.funding.domain.project.service.lifecycle;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.exception.InvalidProjectStatusTransitionException;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.global.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectLifecycleServiceTest {

    private ProjectRepository projectRepository;
    private ProjectLifecycleService lifecycleService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        lifecycleService = new ProjectLifecycleService(projectRepository);
    }

    // ────────────────────────────────────────
    // requestReview
    // ────────────────────────────────────────

    @Test
    void 심사요청_성공() {
        // given: DRAFT 상태, owner=10L, 요청자=10L (본인)
        Project project = draftProject(1L, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.requestReview(1L, 10L);

        assertEquals(ProjectStatus.REVIEW_REQUESTED, project.getStatus());
        verify(projectRepository).findById(1L);
    }

    @Test
    void 심사요청_FUNDING상태면_예외() {
        Project project = projectWithStatus(1L, ProjectStatus.FUNDING, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(InvalidProjectStatusTransitionException.class,
                () -> lifecycleService.requestReview(1L, 10L));
    }

    @Test
    void 심사요청_본인프로젝트_아니면_예외() {
        // owner=10L, 요청자=99L (다른 사람)
        Project project = draftProject(1L, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ApiException.class,
                () -> lifecycleService.requestReview(1L, 99L)); // ✅ 403
    }

    @Test
    void 심사요청_존재하지않는_프로젝트_예외() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> lifecycleService.requestReview(999L, 10L));
    }

    // ────────────────────────────────────────
    // approve / reject (관리자 — owner 검증 없음)
    // ────────────────────────────────────────

    @Test
    void 심사승인_성공() {
        Project project = projectWithStatus(1L, ProjectStatus.REVIEW_REQUESTED, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.approve(1L, 99L);

        assertEquals(ProjectStatus.APPROVED, project.getStatus());
    }

    @Test
    void 심사반려_성공() {
        Project project = projectWithStatus(1L, ProjectStatus.REVIEW_REQUESTED, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.reject(1L, 99L);

        assertEquals(ProjectStatus.REJECTED, project.getStatus());
    }

    @Test
    void 심사승인_DRAFT상태면_예외() {
        Project project = draftProject(1L, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(InvalidProjectStatusTransitionException.class,
                () -> lifecycleService.approve(1L, 99L));
    }

    // ────────────────────────────────────────
    // stop / resume (관리자 — owner 검증 없음)
    // ────────────────────────────────────────

    @Test
    void 강제중단_성공() {
        Project project = projectWithStatus(1L, ProjectStatus.FUNDING, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.stop(1L, 99L);

        assertEquals(ProjectStatus.STOPPED, project.getStatus());
    }

    @Test
    void 재개_성공() {
        Project project = projectWithStatus(1L, ProjectStatus.STOPPED, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.resume(1L, 99L);

        assertEquals(ProjectStatus.FUNDING, project.getStatus());
    }

    @Test
    void 강제중단_APPROVED상태면_예외() {
        Project project = projectWithStatus(1L, ProjectStatus.APPROVED, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(InvalidProjectStatusTransitionException.class,
                () -> lifecycleService.stop(1L, 99L));
    }

    // ────────────────────────────────────────
    // requestDelete / completeDelete
    // ────────────────────────────────────────

    @Test
    void 삭제요청_성공() {
        // owner=10L, 요청자=10L (본인)
        Project project = projectWithStatus(1L, ProjectStatus.FUNDING, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.requestDelete(1L, 10L);

        assertEquals(ProjectStatus.DELETE_REQUESTED, project.getStatus());
    }

    @Test
    void 삭제요청_본인프로젝트_아니면_예외() {
        // owner=10L, 요청자=99L (다른 사람)
        Project project = projectWithStatus(1L, ProjectStatus.FUNDING, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ApiException.class,
                () -> lifecycleService.requestDelete(1L, 99L)); // ✅ 403
    }

    @Test
    void 삭제완료_성공() {
        Project project = projectWithStatus(1L, ProjectStatus.DELETE_REQUESTED, 10L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        lifecycleService.completeDelete(1L);

        assertEquals(ProjectStatus.DELETED, project.getStatus());
    }

    // ────────────────────────────────────────
    // 헬퍼
    // ────────────────────────────────────────

    private Project draftProject(Long id, Long ownerId) {
        return projectWithStatus(id, ProjectStatus.DRAFT, ownerId);
    }

    private Project projectWithStatus(Long id, ProjectStatus status, Long ownerId) {
        // ✅ owner mock — project.getOwner().getId() 가 ownerId 반환
        User mockOwner = mock(User.class);
        when(mockOwner.getId()).thenReturn(ownerId);

        Project p = new Project();
        setField(p, "id", id);
        setField(p, "status", status);
        setField(p, "owner", mockOwner); // ✅ owner 세팅
        return p;
    }

    private static void setField(Object t, String name, Object value) {
        try {
            Field f = t.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(t, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}