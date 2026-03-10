package com.funding.funding.domain.donation.service.cancel;

import com.funding.funding.domain.donation.entity.Donation;
import com.funding.funding.domain.donation.repository.DonationRepository;
import com.funding.funding.domain.donation.status.DonationStatus;
import com.funding.funding.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DonationCancelService {

    private final DonationRepository donationRepository;

    @Transactional
    public void cancel(Long donationId, Long requestUserId) {

        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "후원 내역을 찾을 수 없습니다."));

        // 본인 후원인지 검증
        if (!donation.getUser().getId().equals(requestUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "본인의 후원만 취소할 수 있습니다.");
        }

        if (!donation.getStatus().canTransitionTo(DonationStatus.CANCEL)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "취소할 수 없는 상태의 후원입니다.");
        }

        if (LocalDateTime.now().isAfter(donation.getCancelDeadline())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "취소 가능 기한이 지났습니다.");
        }

        donation.setStatus(DonationStatus.CANCEL);
    }
}