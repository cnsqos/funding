package com.funding.funding.domain.project.facade;

public interface DonationAggregationPort {
	
    long sumSuccessAmountByProjectId(Long projectId);
    // Donation 도메인 구현이 포트 구현시, Project는 DonationRepositort 건들 X
}