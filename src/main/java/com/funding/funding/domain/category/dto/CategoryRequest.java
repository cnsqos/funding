package com.funding.funding.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @NotBlank(message = "카테고리 이름을 입력해주세요")
    @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
    public String name;
}