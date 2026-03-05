package com.funding.funding.domain.donation.entity;

import com.funding.funding.domain.donation.status.DonationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


// 후원 도메인 엔티티 (상태 기반 후원 관리)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long projectId;

    private Long amount;

    @Enumerated(EnumType.STRING)
    private DonationStatus status;

    private LocalDateTime cancelDeadline;

    private LocalDateTime refundedAt;

    @Version
    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // getter / setter
}	