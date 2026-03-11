package com.funding.funding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funding.funding.domain.category.entity.Category;
import com.funding.funding.domain.category.repository.CategoryRepository;
import com.funding.funding.domain.project.entity.Project;
import com.funding.funding.domain.project.entity.ProjectStatus;
import com.funding.funding.domain.project.repository.ProjectRepository;
import com.funding.funding.domain.user.dto.AuthDtos;
import com.funding.funding.domain.user.entity.*;
import com.funding.funding.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
  전체 시나리오 통합 테스트

  실제 HTTP 요청 → 서비스 → DB 전체 흐름을 검증한다.

  시나리오 1: 회원가입 → 로그인 → 프로젝트 생성 → 심사요청 → 후원
  시나리오 2: 검색 / 필터링 / 페이징
  시나리오 3: 좋아요 / 팔로우
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FullScenarioIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private CategoryRepository categoryRepository;

    private static long SEQ = System.currentTimeMillis();

    // 테스트마다 고유한 이메일 생성
    private String uniqueEmail()    { return "user" + SEQ++ + "@test.com"; }
    private String uniqueNickname() { return "nick" + SEQ++; }

    // ────────────────────────────────────────
    // 시나리오 1: 회원가입 → 로그인 → JWT 발급
    // ────────────────────────────────────────

    @Test
    void 시나리오1_회원가입_로그인_JWT발급() throws Exception {
        String email    = uniqueEmail();
        String nickname = uniqueNickname();
        String password = "password123";

        // 1. 회원가입
        AuthDtos.RegisterReq reg = new AuthDtos.RegisterReq(email, nickname, password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        // 2. 로그인 → 토큰 확인
        AuthDtos.LoginReq login = new AuthDtos.LoginReq(email, password);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    // ────────────────────────────────────────
    // 시나리오 2: 프로젝트 검색 / 필터링 / 페이징
    // ────────────────────────────────────────

    @Test
    void 시나리오2_프로젝트_목록_페이징_기본() throws Exception {
        // 기본 조회 (파라미터 없음)
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.size").value(10)); // 기본 size=10
    }

    @Test
    void 시나리오2_status_필터() throws Exception {
        // FUNDING 상태 프로젝트만 조회
        mockMvc.perform(get("/api/projects")
                        .param("status", "FUNDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 시나리오2_keyword_검색() throws Exception {
        // 제목 키워드 검색
        mockMvc.perform(get("/api/projects")
                        .param("keyword", "카페"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void 시나리오2_복합_필터_페이징() throws Exception {
        // status + keyword + page + size + sort 동시 적용
        mockMvc.perform(get("/api/projects")
                        .param("status", "FUNDING")
                        .param("keyword", "테스트")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void 시나리오2_잘못된_status값_400() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest()); // 존재하지 않는 enum → 400
    }

    // ────────────────────────────────────────
    // 시나리오 3: 인증이 필요한 API — JWT 없으면 401
    // ────────────────────────────────────────

    @Test
    void 시나리오3_토큰없이_심사요청_401() throws Exception {
        // ✅ isForbidden() → isUnauthorized() 수정
        // JwtAuthenticationFilter가 토큰 없는 요청을 401로 처리
        // Spring Security 기본 동작은 403이지만
        // JWT 필터에서 토큰을 검증하면서 먼저 401을 반환
        mockMvc.perform(post("/api/projects/1/review-request"))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void 시나리오3_토큰없이_좋아요_401() throws Exception {
        // ✅ isForbidden() → isUnauthorized() 수정
        mockMvc.perform(post("/api/projects/1/likes"))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void 시나리오3_토큰없이_팔로우_401() throws Exception {
        // ✅ isForbidden() → isUnauthorized() 수정
        mockMvc.perform(post("/api/users/1/follow"))
                .andExpect(status().isUnauthorized()); // 401
    }

    // ────────────────────────────────────────
    // 시나리오 4: 관리자 전용 API — USER 토큰으로 접근하면 403
    // ────────────────────────────────────────

    @Test
    void 시나리오4_일반유저가_approve_호출_403() throws Exception {
        // USER 권한으로 로그인
        String token = loginAsUser();

        mockMvc.perform(post("/api/projects/1/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void 시나리오4_일반유저가_reject_호출_403() throws Exception {
        String token = loginAsUser();

        mockMvc.perform(post("/api/projects/1/reject")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ────────────────────────────────────────
    // 시나리오 5: 좋아요 카운트 — 비로그인도 가능
    // ────────────────────────────────────────

    @Test
    void 시나리오5_좋아요_수_조회_비로그인_가능() throws Exception {
        // DB에 프로젝트 직접 생성
        Project project = makePublicProject();
        Long projectId = project.getId();

        mockMvc.perform(get("/api/projects/" + projectId + "/likes/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0")); // 초기값 0
    }

    // ────────────────────────────────────────
    // 헬퍼
    // ────────────────────────────────────────

    // USER 권한으로 회원가입 + 로그인 → 토큰 반환
    private String loginAsUser() throws Exception {
        String email    = uniqueEmail();
        String password = "password123";

        AuthDtos.RegisterReq reg = new AuthDtos.RegisterReq(email, uniqueNickname(), password);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        AuthDtos.LoginReq login = new AuthDtos.LoginReq(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        // JSON에서 accessToken 추출
        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data").get("accessToken").asText();
    }

    // 테스트용 프로젝트 DB에 직접 생성
    private Project makePublicProject() {
        User owner = userRepository.save(new User(
                uniqueEmail(), uniqueNickname(), "pw",
                UserRole.USER, UserStatus.ACTIVE, AuthProvider.LOCAL
        ));

        Category category = new Category();
        setField(category, "name", "카테고리" + SEQ++);
        categoryRepository.save(category);

        Project project = Project.create(
                owner, category,
                "테스트 프로젝트" + SEQ++, "본문",
                100_000L, LocalDateTime.now().plusDays(30)
        );
        setField(project, "status", ProjectStatus.FUNDING);
        return projectRepository.save(project);
    }

    private static void setField(Object t, String name, Object value) {
        try {
            Class<?> clazz = t.getClass();
            while (clazz != null) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(t, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}