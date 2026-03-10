package com.funding.funding.domain.project.repository;

import com.funding.funding.domain.project.entity.Like;
import com.funding.funding.domain.project.entity.LikeId;
import com.funding.funding.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    // 특정 프로젝트의 좋아요 수
    long countByIdProjectId(Long projectId);

    // 이미 좋아요 눌렀는지 확인
    boolean existsByIdUserIdAndIdProjectId(Long userId, Long projectId);

    // 내가 찜한 프로젝트 목록 조회 (마이페이지 찜 목록)
    @Query("SELECT l.project FROM Like l WHERE l.id.userId = :userId ORDER BY l.createdAt DESC")
    List<Project> findLikedProjectsByUserId(@Param("userId") Long userId);
}