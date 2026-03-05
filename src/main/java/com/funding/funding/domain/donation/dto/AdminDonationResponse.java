package com.funding.funding.domain.donation.dto;

import com.funding.funding.domain.donation.status.DonationStatus;

import java.time.LocalDateTime;

public record AdminDonationResponse(
        Long id,
        Long userId,
        Long projectId,
        Long amount,
        DonationStatus status,
        LocalDateTime createdAt,
        LocalDateTime refundedAt
) {}