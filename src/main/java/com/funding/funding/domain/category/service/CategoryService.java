package com.funding.funding.domain.category.service;

import com.funding.funding.domain.category.dto.CategoryRequest;
import com.funding.funding.domain.category.entity.Category;
import com.funding.funding.domain.category.repository.CategoryRepository;
import com.funding.funding.global.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 전체 조회
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // 생성
    @Transactional
    public Category create(CategoryRequest req) {
        Category category = new Category(req.name);
        return categoryRepository.save(category);
    }

    // 수정
    @Transactional
    public Category update(Long id, CategoryRequest req) {
        Category category = findById(id);
        category.updateName(req.name);
        return category;
    }

    // 삭제
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다"));
    }
}