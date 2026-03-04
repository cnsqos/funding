package com.funding.funding.domain.project.service.update;

import org.springframework.stereotype.Service;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.repository.ProjectRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjectUpdateService {

    private final ProjectRepository projectRepository;

    public ProjectUpdateService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public void updateProject(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        // ✅ 수정 제한 정책 적용
        project.validateEditable();

        // 실제 수정 로직은 나중에 DTO 들어오면 추가
        // TODO: 실제 수정 필드 적용은 다음 단계
        // project.changeTitle(request.title());
        // project.changeContent(request.content());
        // project.changeGoalAmount(request.goalAmount());
    }
}