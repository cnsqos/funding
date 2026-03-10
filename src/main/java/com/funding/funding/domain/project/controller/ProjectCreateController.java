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

import com.funding.funding.domain.project.dto.ProjectCreateRequest;
import com.funding.funding.domain.project.service.create.ProjectCreateService;

@RestController
@RequestMapping("/api/projects")
public class ProjectCreateController {

    private final ProjectCreateService projectCreateService;

    public ProjectCreateController(ProjectCreateService projectCreateService) {
        this.projectCreateService = projectCreateService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE) 
    public ResponseEntity<Long> createProject(
            @RequestPart("request") ProjectCreateRequest request,
            @RequestPart("images") List<MultipartFile> images // 이미지를 여러개 받기 위함
    ) {
        Long projectId = projectCreateService.create(request, images);

        return ResponseEntity
                .created(URI.create("/api/projects/" + projectId))
                .body(projectId);
    }
}