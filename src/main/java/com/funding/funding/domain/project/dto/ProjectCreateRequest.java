package com.funding.funding.domain.project.dto;

import lombok.Getter;

import java.time.LocalDateTime;

// 역할 : POST / Projects 할 때 JSON으로 들어오는 값을 스프링이 이 객체에 담는다
// 컨트롤러가 요청 값을 받기 위한 DTO

@Getter
public class ProjectCreateRequest { // 사용자가 보낸 JSON 데이터를 스프링이 객체로 변환해서 넣어줌
    // 목표금액
    private Long goalAmount;
    // 펀딩 시작 시간
    private LocalDateTime startAt;
    // 펀딩 종료 시간
    private LocalDateTime deadline; // 새로 추가
    private Integer thumbnailIndex; // 대표 이미지 번호 (0,1,2번 중 하나)
    
    private Long categoryId; // 추가
    private String title; // 추가
    private String content; // 추가
    private Long ownerId; // 추가
}