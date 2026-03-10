package com.funding.funding.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public class SuspendRequest {

    @NotBlank(message = "정지 사유를 입력해주세요")
    public String reason;
}