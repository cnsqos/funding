package com.funding.funding.domain.project.service.create;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*
imageStorageService가 하는 일
파일 검증(20MB 제한)
파일 이름(UUID 생성)
로컬 폴더에 저장
/uploads/projects/파일이름.png를 반환 -> DB에 저장
*/

@Service
public class ImageStorageService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final String UPLOAD_DIR = "uploads/projects/";

    public String save(MultipartFile file) {
        validate(file);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (Files.notExists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String extension = extractExtension(originalName);
            String savedName = UUID.randomUUID() + extension;

            Path target = uploadPath.resolve(savedName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/projects/" + savedName;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("빈 파일입니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("파일 크기 20MB 초과");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
        }
    }

    private String extractExtension(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }

        int lastDotIndex = originalName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }

        return originalName.substring(lastDotIndex);
    }
}