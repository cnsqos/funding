package com.funding.funding.domain.donation.dto;

import com.funding.funding.domain.donation.status.DonationStatus;

import java.time.LocalDateTime;

public record UserDonationResponse(
        Long projectId,
        Long amount,
        DonationStatus status,
        LocalDateTime createdAt
) {}
