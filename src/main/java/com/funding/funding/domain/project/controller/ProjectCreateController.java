package com.funding.funding.domain.project.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.funding.funding.domain.project.dto.ProjectCreateRequest;
import com.funding.funding.domain.project.service.create.ProjectCreateService;

@RestController
@RequestMapping("/api/projects")
public class ProjectCreateController {

    private final ProjectCreateService projectCreateService;
    private final ObjectMapper objectMapper; // Swagger 테스트용 JSON 파싱

    public ProjectCreateController(ProjectCreateService projectCreateService, ObjectMapper objectMapper) {
        this.projectCreateService = projectCreateService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProject(
            @RequestPart("request") ProjectCreateRequest request,
            @RequestPart("images") List<MultipartFile> images
    ) {
        Long projectId = projectCreateService.create(request, images);

        return ResponseEntity
                .created(URI.create("/api/projects/" + projectId))
                .body(projectId);
    }

    // Swagger에서 테스트하기 위한 임시 API
    @PostMapping(value = "/swagger-test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProjectSwaggerTest(
            @RequestPart("request") String requestJson,
            @RequestPart("images") List<MultipartFile> images
    ) {
        try {
            ProjectCreateRequest request =
                    objectMapper.readValue(requestJson, ProjectCreateRequest.class);

            Long projectId = projectCreateService.create(request, images);

            return ResponseEntity
                    .created(URI.create("/api/projects/" + projectId))
                    .body(projectId);

        } catch (Exception e) {
            throw new RuntimeException("Swagger 테스트용 JSON 파싱 실패", e);
        }
    }
}