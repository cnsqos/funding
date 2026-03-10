package com.funding.funding.domain.project.service.create;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.funding.funding.domain.category.entity.Category;
import com.funding.funding.domain.category.repository.CategoryRepository;
import com.funding.funding.domain.project.dto.ProjectCreateRequest;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectImage;
import com.funding.funding.domain.project.repository.ProjectImageRepository;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.repository.UserRepository;

import jakarta.transaction.Transactional;

// 프로젝트 생성 로직 담당
// Controller가 요청을 받으면 실제 일을 하는 곳

@Service
public class ProjectCreateService { // Spring이 자동으로 Bean으로 등록해서 관리

    // DB에 저장하기 위한 Repository
    private final ProjectRepository projectRepository;
    private final ProjectImageRepository projectImageRepository;
    private final ImageStorageService imageStorageService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // 생성자 주입
    public ProjectCreateService(ProjectRepository projectRepository,
                                ProjectImageRepository projectImageRepository,
                                ImageStorageService imageStorageService,
                                CategoryRepository categoryRepository,
                                UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectImageRepository = projectImageRepository;
        this.imageStorageService = imageStorageService;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional // 이 메서드 안의 DB작업을 하나의 작업 단위로 묶기 위함
                   // 이미지를 같이 받기 위해 바뀜
    public Long create(ProjectCreateRequest req, List<MultipartFile> images) { // 프로젝트 생성 로직
        validateRequest(req);
        validateImages(images, req.getThumbnailIndex());

        // owner 조회
        User owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // category 조회
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 카테고리입니다."));

        // Project 엔티티의 정식 생성 메서드 사용
        Project project = Project.create(
                owner,
                category,
                req.getTitle(),
                req.getContent(),
                req.getGoalAmount(),
                req.getDeadline()
        );

        // 시작 예약일 세팅 (null이면 안 넣음)
        if (req.getStartAt() != null) {
            project.scheduleStart(req.getStartAt());
        }

        Project saved = projectRepository.save(project); // 프로젝트 저장

        for (int i = 0; i < images.size(); i++) {
            MultipartFile imageFile = images.get(i);

            String imageUrl = imageStorageService.save(imageFile); // 로컬 폴더에 저장 (나중에 S3로 바꿔도 서비스는 그대로 사용 가능)
            boolean isThumbnail = (i == req.getThumbnailIndex());

            ProjectImage projectImage = new ProjectImage(saved, imageUrl, isThumbnail); // DB에 저장할 엔티티를 만듦
            projectImageRepository.save(projectImage);
        }

        return saved.getId();
    }

    private void validateRequest(ProjectCreateRequest req) {
        if (req.getOwnerId() == null) {
            throw new RuntimeException("ownerId는 필수입니다.");
        }

        if (req.getCategoryId() == null) {
            throw new RuntimeException("categoryId는 필수입니다.");
        }

        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new RuntimeException("title은 필수입니다.");
        }

        if (req.getContent() == null || req.getContent().isBlank()) {
            throw new RuntimeException("content는 필수입니다.");
        }

        if (req.getGoalAmount() == null || req.getGoalAmount() <= 0) {
            throw new RuntimeException("goalAmount는 0보다 커야 합니다.");
        }

        if (req.getDeadline() == null) {
            throw new RuntimeException("deadline은 필수입니다.");
        }
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