package com.funding.funding.domain.user.dto;

import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    public String nickname;

    public String profileImage;
}