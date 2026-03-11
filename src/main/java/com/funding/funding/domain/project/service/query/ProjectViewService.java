package com.funding.funding.domain.project.service.query;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectDailyView;
import com.funding.funding.domain.project.repository.ProjectDailyViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/*
 프로젝트 상세 조회 시 일별 조회수를 집계한다.
 */
@Service
@RequiredArgsConstructor
public class ProjectViewService {

    private final ProjectDailyViewRepository projectDailyViewRepository;

    @Transactional
    public void increaseView(Project project) {
        LocalDate today = LocalDate.now();

        ProjectDailyView dailyView = projectDailyViewRepository
                .findByProjectAndViewDate(project, today)
                .orElse(null);

        if (dailyView == null) {
            projectDailyViewRepository.save(ProjectDailyView.create(project, today));
            return;
        }

        dailyView.increase();
        projectDailyViewRepository.save(dailyView);
    }
}