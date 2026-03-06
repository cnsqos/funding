package com.funding.funding.domain.project.controller;

import com.funding.funding.domain.project.dto.ProjectCreateRequest;
import com.funding.funding.domain.project.service.create.ProjectCreateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/projects")
public class ProjectCreateController {

    private final ProjectCreateService projectCreateService;

    public ProjectCreateController(ProjectCreateService projectCreateService) {
        this.projectCreateService = projectCreateService;
    }

    @PostMapping
    public ResponseEntity<Long> createProject(@RequestBody ProjectCreateRequest request) {
        Long projectId = projectCreateService.create(request);

        return ResponseEntity
                .created(URI.create("/api/projects/" + projectId))
                .body(projectId);
    }
}