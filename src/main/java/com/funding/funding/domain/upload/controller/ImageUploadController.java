package com.funding.funding.domain.upload.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {

    private static final String UPLOAD_DIR = "uploads/projects/";

    @PostMapping("/images")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {

        // 파일명 생성
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // 저장 경로
        Path uploadPath = Paths.get(UPLOAD_DIR);

        // 폴더 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 실제 저장 위치
        Path filePath = uploadPath.resolve(fileName);

        // 파일 저장
        file.transferTo(filePath);

        // 프론트에 전달할 URL
        String url = "/uploads/projects/" + fileName;

        return Map.of("url", url);
    }
}