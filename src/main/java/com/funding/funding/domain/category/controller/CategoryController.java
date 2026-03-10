package com.funding.funding.domain.category.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.funding.funding.domain.category.dto.CategoryRequest;
import com.funding.funding.domain.category.entity.Category;
import com.funding.funding.domain.category.service.CategoryService;
import com.funding.funding.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카테고리", description = "카테고리 목록 및 관리자 CRUD")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // GET /api/categories — 전체 목록 (공개)
    @GetMapping
    public ApiResponse<List<Category>> getAll() {
        return ApiResponse.ok(categoryService.getAll());
    }

    // POST /api/categories — 생성 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<Category> create(@Valid @RequestBody CategoryRequest req) {
        return ApiResponse.ok(categoryService.create(req));
    }

    // PUT /api/categories/{id} — 수정 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<Category> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest req
    ) {
        return ApiResponse.ok(categoryService.update(id, req));
    }

    // DELETE /api/categories/{id} — 삭제 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok(null);
    }
}