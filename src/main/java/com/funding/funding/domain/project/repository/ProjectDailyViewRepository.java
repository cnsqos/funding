package com.funding.funding.domain.project.repository;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectDailyView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/*
 projectdailyview 엔티티를 다루는 Repository(DB에서 찾고 저장)
 기본적인 저장, 조회, 삭제 기능을 자동으로 씀
 */

public interface ProjectDailyViewRepository extends JpaRepository<ProjectDailyView, Long> { 
	
	// 메서드 이름으로 JPA가 쿼리를 만들어주는 방식
    Optional<ProjectDailyView> findByProjectAndViewDate(Project project, LocalDate viewDate);

    @Query("SELECT COALESCE(SUM(pdv.viewCount), 0) FROM ProjectDailyView pdv WHERE pdv.project.id = :projectId")
    long sumViewCountByProjectId(Long projectId);
    
    @Query("""
    	    select coalesce(sum(pdv.viewCount), 0)
    	    from ProjectDailyView pdv
    	    where pdv.project.id = :projectId
    	      and pdv.viewDate between :startDate and :endDate
    	""")
    	long sumViewCountBetween(
    	        @Param("projectId") Long projectId,
    	        @Param("startDate") LocalDate startDate,
    	        @Param("endDate") LocalDate endDate
    	);
    
}