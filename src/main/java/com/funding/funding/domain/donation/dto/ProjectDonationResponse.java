package com.funding.funding.domain.donation.dto;

import com.funding.funding.domain.donation.status.DonationStatus;

import java.time.LocalDateTime;

public record ProjectDonationResponse(
        Long amount,
        LocalDateTime createdAt,
        DonationStatus status) {

}
/*
 * ProjectDonationResponse (DTO)
 *
 * 📌 이 클래스는 프로젝트 작성자가 후원 내역을 조회할 때
 *     필요한 데이터만 전달하기 위한 응답 객체이다.
 *
 * ------------------------------------------------------
 * ✅ 왜 record를 사용하는가?
 *
 * record는 "값을 담는 전용 객체"를 만들기 위한 자바 문법이다.
 *
 * 특징:
 * 1. 모든 필드는 자동으로 final (불변 객체)
 * 2. 생성자 자동 생성
 * 3. getter 자동 생성 (getAmount()가 아니라 amount() 형태)
 * 4. equals(), hashCode(), toString() 자동 생성
 *
 * ------------------------------------------------------
 * ✅ DTO에 record가 적합한 이유
 *
 * DTO는 데이터를 "전달"하는 목적의 객체이다.
 * 값을 변경할 필요가 없기 때문에 불변 객체가 적합하다.
 *
 * → 실수로 값이 변경되는 것을 방지
 * → 멀티스레드 환경에서도 안전
 * → 코드가 훨씬 간결해짐
 *
 * ------------------------------------------------------
 * ⚠ 주의
 *
 * - JPA Entity에는 record를 사용하면 안 된다.
 *   (JPA는 프록시 생성과 필드 변경이 필요하기 때문)
 *
 * - record의 getter는 getXxx()가 아니라
 *   필드이름() 형태로 호출한다.
 *
 *   예:
 *   response.amount()
 *   response.createdAt()
 *   response.status()
 *
 */
