package com.funding.funding.domain.project.repository;

import com.funding.funding.domain.project.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 *  Jpareository를 상속
 *  
 */
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
    Optional<ProjectImage> findByProjectIdAndThumbnailTrue(Long projectId);// 썸네일 조회 메서드
}
