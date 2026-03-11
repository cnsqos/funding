package com.funding.funding.domain.project.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "project_images")
public class ProjectImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Getter
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Getter
    @Column(name = "is_thumbnail", nullable = false)
    private boolean thumbnail;
    
    protected ProjectImage() {
    }

    public ProjectImage(Project project, String imageUrl, boolean thumbnail) { // 생성자 추가
        this.project = project; 
        this.imageUrl = imageUrl;
        this.thumbnail = thumbnail;
    }

}