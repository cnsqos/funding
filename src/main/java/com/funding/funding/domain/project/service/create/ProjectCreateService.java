package com.funding.funding.domain.project.service.create;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.funding.funding.domain.project.dto.ProjectCreateRequest;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectImage;
import com.funding.funding.domain.project.repository.ProjectImageRepository;
import com.funding.funding.domain.project.repository.ProjectRepository;

import jakarta.transaction.Transactional;

// 프로젝트 생성 로직 담당
// Controller가 요청을 받으면 실제 일을 하는 곳

@Service
public class ProjectCreateService { // Spring이 자동으로 Bean으로 등록해서 관리

    // DB에 저장하기 위한 Repository
    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final ImageStorageService imageStorageService;

    // 생성자 주입
    public ProjectCreateService(ProjectRepository projectRepository,
                                ProjectImageRepository projectImageRepository,
                                ImageStorageService imageStorageService) {
        this.projectRepository = projectRepository;
        this.projectImageRepository = projectImageRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional // 이 메서드 안의 DB작업을 하나의 작업 단위로 묶기 위함
    			   // 이미지를 같이 받기 위해 바뀜
    public Long create(ProjectCreateRequest req, List<MultipartFile> images) { // 프로젝트 생성 로직
        validateImages(images, req.getThumbnailIndex());

        Project project = new Project(); // 기본 status = DRAFT

        // 시작 예약일 세팅 (null이면 안 넣음)
        if (req.getStartAt() != null) {
            project.scheduleStart(req.getStartAt());
        }

        if (req.getDeadline() != null) {
            project.scheduleDeadline(req.getDeadline());
        }

        // goalAmount 세팅 (Project에 setter/메서드가 있어야 함)
        project.changeGoalAmount(req.getGoalAmount());

        Project saved = projectRepository.save(project); // 프로젝트 저장

        for (int i = 0; i < images.size(); i++) {
            MultipartFile imageFile = images.get(i);

            String imageUrl = imageStorageService.save(imageFile); // 로컬 폴더에 저장
            boolean isThumbnail = (i == req.getThumbnailIndex());

            ProjectImage projectImage = new ProjectImage(saved, imageUrl, isThumbnail); // DB에 저장할 엔티티를 만듦 
            projectImageRepository.save(projectImage);
        }

        return saved.getId();
    }

    private void validateImages(List<MultipartFile> images, Integer thumbnailIndex) {
        if (images == null || images.isEmpty()) {
            throw new RuntimeException("이미지는 최소 1개 이상 업로드해야 합니다.");
        }

        if (thumbnailIndex == null) {
            throw new RuntimeException("대표 이미지 번호는 필수입니다.");
        }

        if (thumbnailIndex < 0 || thumbnailIndex >= images.size()) {
            throw new RuntimeException("대표 이미지 번호가 올바르지 않습니다.");
        }
    }
}