package com.funding.funding.domain.project.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/*
 조회수 데이터를 저장하는 틀
 어떤 값들을 저장할지
 추가된 기능 : 새 일별 조회수 객체 생성 가능, 조회 수 증가 가능
 */

@Entity
@Table(name = "project_daily_views",
       uniqueConstraints = @UniqueConstraint(name = "uq_pdv_project_date", columnNames = {"project_id", "view_date"}))
public class ProjectDailyView {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    public ProjectDailyView() {}
    
    // 객체 생성시 값 저장
    public ProjectDailyView(Project project, LocalDate viewDate, Integer viewCount) {
        this.project = project;
        this.viewDate = viewDate;
        this.viewCount = viewCount;
    }
    
    // 일별 조회 수 첫 생성 전용 메서드
    public static ProjectDailyView create(Project project, LocalDate viewDate) {
        return new ProjectDailyView(project, viewDate, 1);
    }
    
    // 기존 기록이 있을 때 조회수를 1 올리는 기능
    public void increase() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public LocalDate getViewDate() {
        return viewDate;
    }

    public Integer getViewCount() {
        return viewCount;
    }
}