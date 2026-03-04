package com.funding.funding.domain.project.service.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.project.status.ProjectStatus;

class ProjectUpdateServiceTest {

    @Test
    void DRAFT이면_수정_검증을_통과한다() {
        // given
        ProjectRepository repo = mock(ProjectRepository.class);
        ProjectUpdateService service = new ProjectUpdateService(repo);

        Project project = new Project(); // 기본 DRAFT
        setId(project, 1L);

        when(repo.findById(1L)).thenReturn(Optional.of(project));

        // when & then (예외 없어야 함)
        assertDoesNotThrow(() -> service.updateProject(1L));
        verify(repo).findById(1L);
    }

    @Test
    void DRAFT가_아니면_수정시_예외가_발생한다() {
        // given
        ProjectRepository repo = mock(ProjectRepository.class);
        ProjectUpdateService service = new ProjectUpdateService(repo);

        Project project = new Project();
        setId(project, 1L);

        // DRAFT -> REVIEW_REQUESTED 로 변경 (정책상 수정 불가 상태)
        project.requestReview();

        when(repo.findById(1L)).thenReturn(Optional.of(project));

        // when & then
        assertThrows(IllegalStateException.class, () -> service.updateProject(1L));
        verify(repo).findById(1L);
    }

    // ✅ 테스트에서만 id 세팅 (엔티티에 setter 없으니까)
    private static void setId(Project project, Long id) {
        try {
            Field f = Project.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(project, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}