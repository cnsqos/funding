package com.funding.funding.Donation.Service.OptimisticLock;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.status.DonationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Optimistic Lock 동시성 테스트
 *
 * 목적:
 * - 동일한 Donation을 두 트랜잭션이 동시에 수정할 경우
 *   하나는 실패해야 한다.
 *
 * 검증 포인트:
 * - 두 번째 저장 시 OptimisticLock 예외 발생
 */
@SpringBootTest
class DonationOptimisticLockTest {

    @Autowired
    private DonationRepository donationRepository;

    @Test
    void optimistic_lock_should_fail_on_concurrent_update() {

        // 1. 초기 데이터 생성
        Donation donation = new Donation();
        donation.setStatus(DonationStatus.SUCCESS);
        donation.setCancelDeadline(LocalDateTime.now().plusHours(1));

        donationRepository.saveAndFlush(donation);

        // 2. 서로 다른 영속성 컨텍스트처럼 두 번 조회
        Donation firstRead = donationRepository.findById(donation.getId()).get();
        Donation secondRead = donationRepository.findById(donation.getId()).get();

        // 3. 첫 번째 수정 → 정상 저장
        firstRead.setStatus(DonationStatus.CANCEL);
        donationRepository.saveAndFlush(firstRead);

        // 4. 두 번째 수정 시도 → 버전 충돌 발생해야 함
        secondRead.setStatus(DonationStatus.REFUND);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            donationRepository.saveAndFlush(secondRead);
        });
    }
}