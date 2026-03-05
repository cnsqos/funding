package com.funding.funding.Donation.Service.Refund;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.service.refund.DonationRefundService;
import com.funding.funding.domain.donation.status.DonationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class DonationRefundServiceTest {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private DonationRefundService refundService;


    /*
     * 정상 케이스 테스트
     *
     * 목적:
     * - SUCCESS 상태의 후원이 REFUND로 정상 전이되는지 확인
     * - 환불 처리 시간이 기록되는지 확인
     *
     * 검증 포인트:
     * - 상태가 REFUND로 변경되었는가?
     * - refundedAt 값이 null이 아닌가?
     */
    @Test
    void success_to_refund() {

        // given: SUCCESS 상태의 후원 생성
        Donation donation = new Donation();
        donation.setStatus(DonationStatus.SUCCESS);

        donationRepository.save(donation);

        // when: 환불 서비스 실행
        refundService.refund(donation.getId());

        // then: 상태와 환불 시간 검증
        Donation result = donationRepository.findById(donation.getId()).get();

        assertEquals(DonationStatus.REFUND, result.getStatus());
        assertNotNull(result.getRefundedAt());
    }

    /*
     * 실패 케이스 테스트
     *
     * 목적:
     * - 이미 REFUND 상태인 후원에 대해
     *   다시 refund를 호출하면 예외가 발생하는지 확인
     *
     * 검증 포인트:
     * - IllegalStateException 발생 여부
     */
    @Test
    void refund_fail_when_already_refunded() {

        // given: 이미 REFUND 상태의 후원
        Donation donation = new Donation();
        donation.setStatus(DonationStatus.REFUND);

        donationRepository.save(donation);

        // when & then: 환불 재요청 시 예외 발생
        assertThrows(IllegalStateException.class,
                () -> refundService.refund(donation.getId()));
    }
}