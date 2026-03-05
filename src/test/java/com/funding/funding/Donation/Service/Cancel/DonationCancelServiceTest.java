package com.funding.funding.Donation.Service.Cancel;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.service.cancel.DonationCancelService;
import com.funding.funding.domain.donation.status.DonationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/*
 * DonationCancelService 테스트
 *
 * 목적:
 * - SUCCESS → CANCEL 상태 전이 검증
 * - 24시간 제한 조건 검증
 * - 정책 위반 시 예외 발생 확인
 */
@SpringBootTest
@Transactional
class DonationCancelServiceTest {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private DonationCancelService cancelService;

    /*
     * 정상 케이스
     *
     * 조건:
     * - 상태: SUCCESS
     * - cancelDeadline: 현재 시간 이후
     *
     * 기대 결과:
     * - 상태가 CANCEL로 변경되어야 한다.
     */
    @Test
    void success_to_cancel_within_deadline() {

        // given: SUCCESS 상태 + 취소 가능 시간 남음
        Donation donation = new Donation();
        donation.setStatus(DonationStatus.SUCCESS);
        donation.setCancelDeadline(LocalDateTime.now().plusHours(1));

        donationRepository.save(donation);

        // when: 취소 요청
        cancelService.cancel(donation.getId());

        // then: 상태가 CANCEL로 변경되었는지 확인
        Donation result = donationRepository.findById(donation.getId()).get();
        assertEquals(DonationStatus.CANCEL, result.getStatus());
    }

    /*
     * 실패 케이스
     *
     * 조건:
     * - 상태: SUCCESS
     * - cancelDeadline: 이미 지난 시간
     *
     * 기대 결과:
     * - IllegalStateException 발생
     * - 상태는 변경되지 않아야 한다.
     */
    @Test
    void cancel_fail_when_deadline_passed() {

        // given: SUCCESS 상태지만 취소 가능 시간 초과
        Donation donation = new Donation();
        donation.setStatus(DonationStatus.SUCCESS);
        donation.setCancelDeadline(LocalDateTime.now().minusHours(1));

        donationRepository.save(donation);

        // when & then: 취소 시 예외 발생
        assertThrows(IllegalStateException.class,
                () -> cancelService.cancel(donation.getId()));
    }


    /*
     * 실패 케이스
     *
     * 조건:
     * - 이미 CANCEL 상태
     *
     * 기대 결과:
     * - IllegalStateException 발생
     */
    @Test
    void cancel_fail_when_already_cancelled() {

        // given: 이미 CANCEL 상태
        Donation donation = new Donation();
        donation.setStatus(DonationStatus.CANCEL);
        donation.setCancelDeadline(LocalDateTime.now().plusHours(1));

        donationRepository.save(donation);

        // when & then
        assertThrows(IllegalStateException.class,
                () -> cancelService.cancel(donation.getId()));
    }
}