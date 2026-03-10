package com.funding.funding.domain.user.dto;

import com.funding.funding.domain.user.entity.User;

public class UserProfileResponse {

    public Long id;
    public String email;
    public String nickname;
    public String profileImage;
    public String role;
    public String status;

    public static UserProfileResponse from(User u) {
        UserProfileResponse r = new UserProfileResponse();
        r.id           = u.getId();
        r.email        = u.getEmail();
        r.nickname     = u.getNickname();
        r.profileImage = u.getProfileImage();
        r.role         = u.getRole().name();
        r.status       = u.getStatus().name();
        return r;
    }
}