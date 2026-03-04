package com.funding.funding.domain.project.entity;

import java.time.LocalDateTime;

import com.funding.funding.domain.project.status.ProjectStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity // 이 클래스가 DB 테이블과 연결되는 클래스
@Table(name = "project_status_logs") // 이 클래스가 연결된 DB 테이블 이름
public class ProjectStatusLog {

    @Id // DB에서 project_status_logs.id 컬럼과 연결
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID값을 DB가 자동 생성하도록 한다
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "project_id", nullable = false) // Java 필드와 DB 컬럼을 매핑
    private Long projectId;

    @Enumerated(EnumType.STRING) // 
    @Column(name = "before_status", nullable = false, length = 30) // nullable -> DB 구조와 코드 구조가 명확해짐
    private ProjectStatus beforeStatus; 

    @Enumerated(EnumType.STRING) // enum을 DB에 저장하는 방식 지정, 문자열 그대로 저장, 로그 데이터이기 때문에 사람이 읽을 수 있어야 한다
    @Column(name = "after_status", nullable = false, length = 30) 
    private ProjectStatus afterStatus; 

    @Column(name = "changed_by", nullable = false, length = 20)
    private String changedBy; // "USER" or "ADMIN" enum 대신 String을 쓰는 이유 
    						  // String을 써도 나중에 enum으로 변경이 쉽고 유연성에 있어서 더 좋기 때문에 String을 사용 -> 나중에 enum으로 바꿔도 상관 없음				
    @Column(name = "changed_by_id", nullable = false)
    private Long changedById; // 상태 변경을 수행한 사용자 ID

    @Column(name = "created_at", nullable = false) // 상태 변경이 일어난 시간
    private LocalDateTime createdAt; // 로그는 누가/언제/어떤 상태로 변경했는지가 중요

    protected ProjectStatusLog() {
        // JPA는 객체를 생성할 때 리플렉션을 사용
    	// JPA 기본 생성자가 반드시 필요
    }

    public ProjectStatusLog( // 로그를 생성할 때 필요한 값들을 한 번에 넣는다
            Long projectId,  // setter 대신 생성자를 쓰는가 : 로그는 한번 생성되면 수정 X 
            ProjectStatus beforeStatus,
            ProjectStatus afterStatus,
            String changedBy,
            Long changedById,
            LocalDateTime createdAt
    ) {
        this.projectId = projectId;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.changedBy = changedBy;
        this.changedById = changedById;
        this.createdAt = createdAt;
    }

    public Long getId() { // 로그 조회용 메서드, 로그는 읽기 전용 데이터라서 setter는 생성 X
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public ProjectStatus getBeforeStatus() {
        return beforeStatus;
    }

    public ProjectStatus getAfterStatus() {
        return afterStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Long getChangedById() {
        return changedById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}