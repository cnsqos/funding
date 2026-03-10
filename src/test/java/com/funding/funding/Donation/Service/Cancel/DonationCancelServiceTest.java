package com.funding.funding.Donation.Service.Cancel;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.service.cancel.DonationCancelService;
import com.funding.funding.domain.donation.status.DonationStatus;
import com.funding.funding.domain.user.entity.User;
import com.funding.funding.global.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DonationCancelServiceTest {

    private DonationRepository donationRepository;
    private DonationCancelService cancelService;

    @BeforeEach
    void setUp() {
        donationRepository = mock(DonationRepository.class);
        cancelService = new DonationCancelService(donationRepository);
    }

    /*
      정상 케이스
      - 상태: SUCCESS, cancelDeadline: 현재 이후, 본인 후원
      - 기대: 상태가 CANCEL로 변경
     */
    @Test
    void success_to_cancel_within_deadline() {
        // given
        Donation donation = createDonation(1L, DonationStatus.SUCCESS, LocalDateTime.now().plusHours(1), 1L);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        // when
        cancelService.cancel(1L, 1L); // requestUserId 추가

        // then
        assertEquals(DonationStatus.CANCEL, donation.getStatus());
    }

    /*
      실패 케이스: 취소 마감 시간 초과
     */
    @Test
    void cancel_fail_when_deadline_passed() {
        // given
        Donation donation = createDonation(1L, DonationStatus.SUCCESS, LocalDateTime.now().minusHours(1), 1L);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        // when & then
        assertThrows(ApiException.class, () -> cancelService.cancel(1L, 1L));
    }

    /*
      실패 케이스: 이미 CANCEL 상태
     */
    @Test
    void cancel_fail_when_already_cancelled() {
        // given
        Donation donation = createDonation(1L, DonationStatus.CANCEL, LocalDateTime.now().plusHours(1), 1L);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        // when & then
        assertThrows(ApiException.class, () -> cancelService.cancel(1L, 1L));
    }

    /*
      실패 케이스: PENDING 상태
     */
    @Test
    void cancel_fail_when_status_is_pending() {
        // given
        Donation donation = createDonation(1L, DonationStatus.PENDING, LocalDateTime.now().plusHours(1), 1L);
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        // when & then
        assertThrows(ApiException.class, () -> cancelService.cancel(1L, 1L));
    }

    /*
      실패 케이스: 존재하지 않는 후원
     */
    @Test
    void cancel_fail_when_donation_not_found() {
        // given
        when(donationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ApiException.class, () -> cancelService.cancel(999L, 1L));
    }

    /*
      실패 케이스: 다른 사람의 후원 취소 시도 ✅ 신규
      - donationOwnerId: 2L, requestUserId: 1L
      - 기대: ApiException(403) 발생
     */
    @Test
    void cancel_fail_when_not_owner() {
        // given
        Donation donation = createDonation(1L, DonationStatus.SUCCESS, LocalDateTime.now().plusHours(1), 2L); // 주인은 2L
        when(donationRepository.findById(1L)).thenReturn(Optional.of(donation));

        // when & then — 1L이 2L의 후원을 취소하려 함
        assertThrows(ApiException.class, () -> cancelService.cancel(1L, 1L));
    }

    // ────────────────────────────────────────
    // 헬퍼
    // ────────────────────────────────────────

    private Donation createDonation(Long id, DonationStatus status,
                                    LocalDateTime cancelDeadline, Long ownerId) {
        // User mock — donation.getUser().getId() 가 ownerId 반환하도록
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(ownerId);

        Donation donation = new Donation();
        setField(donation, "id", id);
        setField(donation, "user", mockUser); // user 세팅
        donation.setStatus(status);
        donation.setCancelDeadline(cancelDeadline);
        return donation;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}