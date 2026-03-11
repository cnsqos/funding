package com.funding.funding.domain.user.service.user;

import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.repository.LikeRepository;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.user.dto.UserMeRes;
import com.funding.funding.domain.user.dto.UserProfileUpdateRequest;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.domain.user.repository.UserRepository;
import com.funding.funding.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LikeRepository likeRepository;

    public UserService(UserRepository userRepository,
                       ProjectRepository projectRepository,
                       LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.likeRepository = likeRepository;
    }

    // ── 내 정보 조회 ──────────────────────────────────
    // 기존에는 User 엔티티 자체를 반환했지만,
    // 이제는 프론트에 필요한 값만 내려주기 위해 DTO로 변환해서 반환
    public UserMeRes getMe(Long userId) {
        User user = findUser(userId);
        return UserMeRes.from(user);
    }

    // ── 프로필 수정 ───────────────────────────────────
    @Transactional
    public User updateProfile(Long userId, UserProfileUpdateRequest req) {
        User user = findUser(userId);

        // 닉네임 중복 체크 (본인 제외)
        if (req.nickname != null && !req.nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(req.nickname)) {
                throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다");
            }
        }

        user.updateProfile(req.nickname, req.profileImage);
        return user;
    }

    // ── 내 프로젝트 목록 ──────────────────────────────
    public List<Project> getMyProjects(Long userId) {
        return projectRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
    }

    // ── 찜 목록 ───────────────────────────────────────
    public List<Project> getLikedProjects(Long userId) {
        return likeRepository.findLikedProjectsByUserId(userId);
    }

    // ── 관리자: 회원 정지 ─────────────────────────────
    @Transactional
    public void suspend(Long userId, String reason) {
        User user = findUser(userId);

        if (user.getRole().name().equals("ADMIN")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "관리자 계정은 정지할 수 없습니다");
        }

        user.suspend(reason);
    }

    // ── 관리자: 정지 해제 ─────────────────────────────
    @Transactional
    public void activate(Long userId) {
        User user = findUser(userId);
        user.activate();
    }

    // ── 관리자: 회원 목록 ─────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ── 공통 ──────────────────────────────────────────
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"));
    }
}