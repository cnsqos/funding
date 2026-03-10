package com.funding.funding.domain.project.repository;

import com.funding.funding.domain.project.entity.ProjectImage;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 *  Jpareository를 상속
 *  
 */
public interface ProjectImageRepository extends JpaRepository<ProjectImage, Long> {
}
