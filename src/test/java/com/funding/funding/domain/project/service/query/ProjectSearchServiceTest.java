package com.funding.funding.domain.project.service.query;

import com.funding.funding.domain.project.dto.ProjectSummaryResponse;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.repository.LikeRepository;
import com.funding.funding.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// ProjectQueryService의 검색 로직 단위 테스트
class ProjectSearchServiceTest {

    private ProjectRepository projectRepository;
    private LikeRepository likeRepository;
    private ProjectViewService projectViewService;
    private ProjectQueryService queryService;

    @BeforeEach
    void setUp() {
        projectRepository  = mock(ProjectRepository.class);
        likeRepository     = mock(LikeRepository.class);
        projectViewService = mock(ProjectViewService.class);
        queryService = new ProjectQueryService(projectRepository, likeRepository, projectViewService);

        // ✅ likeRepository mock 기본값 — 서비스 내부에서 트랜잭션 안에 매핑 시 호출됨
        when(likeRepository.countByIdProjectId(anyLong())).thenReturn(0L);

        // ✅ searchOrderByLikes mock 기본값
        when(projectRepository.searchOrderByLikes(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
    }

    @Test
    void 전체_조회_파라미터_없으면_null로_search_호출() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.search(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        // when — 반환 타입이 Page<ProjectSummaryResponse>로 변경됨
        Page<ProjectSummaryResponse> result = queryService.search(null, null, null, null, null, pageable);

        // then
        assertNotNull(result);
        verify(projectRepository).search(null, null, null, null, pageable);
    }

    @Test
    void status_필터_적용() {
        Pageable pageable = PageRequest.of(0, 10);
        Project p = makeProject(ProjectStatus.FUNDING);
        when(projectRepository.search(ProjectStatus.FUNDING, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(p)));

        Page<ProjectSummaryResponse> result = queryService.search(ProjectStatus.FUNDING, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void keyword가_빈문자열이면_null로_변환되어_search_호출() {
        // 빈 keyword는 null로 변환되어야 전체 검색으로 처리됨
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.search(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        queryService.search(null, null, "   ", null, null, pageable);

        verify(projectRepository).search(null, null, null, null, pageable);
    }

    @Test
    void keyword가_있으면_trim되어_전달() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.search(null, null, "카페", null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        queryService.search(null, null, "  카페  ", null, null, pageable);

        verify(projectRepository).search(null, null, "카페", null, pageable);
    }

    @Test
    void tagName이_있으면_태그_필터_적용() {
        Pageable pageable = PageRequest.of(0, 10);
        Project p = makeProject(ProjectStatus.FUNDING);
        when(projectRepository.search(null, null, null, "환경", pageable))
                .thenReturn(new PageImpl<>(List.of(p)));

        Page<ProjectSummaryResponse> result = queryService.search(null, null, null, "환경", null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(projectRepository).search(null, null, null, "환경", pageable);
    }

    @Test
    void tagName이_빈문자열이면_null로_변환() {
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.search(null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        queryService.search(null, null, null, "   ", null, pageable);

        verify(projectRepository).search(null, null, null, null, pageable);
    }

    @Test
    void sortBy가_likes이면_searchOrderByLikes_호출() {
        Pageable pageable = PageRequest.of(0, 10);

        queryService.search(null, null, null, null, "likes", pageable);

        verify(projectRepository).searchOrderByLikes(null, null, null, null, PageRequest.of(0, 10));
        verify(projectRepository, never()).search(any(), any(), any(), any(), any());
    }

    private Project makeProject(ProjectStatus status) {
        Project p = new Project();
        setField(p, "status", status);
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