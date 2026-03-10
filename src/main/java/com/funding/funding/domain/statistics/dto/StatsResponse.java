package com.funding.funding.domain.statistics.dto;

public class StatsResponse {

    public long totalUsers;           // 전체 회원 수
    public long activeUsers;          // 활성 회원 수
    public long suspendedUsers;       // 정지 회원 수

    public long totalProjects;        // 전체 프로젝트 수
    public long fundingProjects;      // 펀딩 중
    public long successProjects;      // 성공
    public long failedProjects;       // 실패

    public long totalDonations;       // 전체 후원 건수
    public long successDonationAmount; // 성공 후원 총액 (원)
}