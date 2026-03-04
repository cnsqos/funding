package com.funding.funding.domain.project.status;

public enum ProjectStatus {
	DRAFT,				// 작성 중(생성, 작성자 수정/삭제, 심사요청 X, 후원 불가)
	REVIEW_REQUESTED,	// 심사 요청됨(심사 요청, 관리자 승인/반려 대기 중, 수정/삭제 불가, 후원 불가)
	REJECTED,			// 심사 반려(심사 거절, 반려사유 O, 수정 가능, 수정 후 재심사 요청 가능, 후원 불가)
	APPROVED,			// 심사 승인(심사 통과, 펀딩 시작 전, 시작일 도달 시 FUNDING으로 전환, 수정 시 REVIEW_REQUESTED로 돌아감, 후원 불가)
    FUNDING,			// 후원 진행 중(펀딩 시작, 수정 불가, 후원 가능, 마감일 도달 시 SUCCESS 또는 FAILED로 전환, 관리자 강제 중단 가능)
    SUCCESS,			// 목표 달성 종료(마감일 목표 금액 달성, 후원 종료, 수정 불가, 정상 종료 상태)
    FAILED,				// 목표 미달 종료(마감일 목표 금액 미달, 후원 종료, 수정 불가, 환불 대상(Donation 도메인 처리)
    STOPPED,			// 관리자 강제 중단(관리자 판단 중단, 후원 불가, 수정 불가, 관리자 승인 시 FUNDING으로 재개 가능)
    DELETE_REQUESTED,	// 삭제 요청됨(사용자 삭제 요청, 환불 절차 진행, 수정 불가, 환불 완료 시 DELETED로 전환)
    DELETED				// 삭제 완료됨(환불 완료, 최종 삭제 상태, 사용자 접근 불가, 논리 삭제)
}
