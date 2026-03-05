package com.funding.funding.domain.donation.status;


    /*
     * DonationStatus
     *
     * 후원의 상태를 정의한다.
     *
     * 상태 목록:
     * - PENDING : 결제 요청 상태 (아직 결제 완료 아님)
     * - SUCCESS : 결제 완료 상태
     * - FAILED  : 결제 실패 상태
     * - CANCEL  : 사용자가 취소한 상태
     * - REFUND  : 환불 완료 상태
     *
     * 이 enum은 "상태 값"만 가지는 것이 아니라
     * 상태 전이 가능 여부도 함께 관리한다.
     */

public enum DonationStatus {
	PENDING,
    SUCCESS,
    FAILED,
    CANCEL,
    REFUND;
    /*
     * 현재 상태(this)에서 target 상태로 이동이 가능한지 검증한다.
     *
     * 예:
     * PENDING → SUCCESS 가능
     * PENDING → FAILED 가능
     * SUCCESS → CANCEL 가능
     * SUCCESS → REFUND 가능
     * 그 외 전이는 모두 불가능
     *
     * @param target 이동하려는 다음 상태
     * @return 이동 가능하면 true, 아니면 false
     */
    
    
    public boolean canTransitionTo(DonationStatus target) {
    	
        switch (this) {
            case PENDING:
                return target == SUCCESS || target == FAILED;
            // 현재 상태(this)가 PENDING이면 SUCCESS 또는 FAILED로만 이동 가능
                
            case SUCCESS:
                return target == CANCEL || target == REFUND;
            // 현재 상태(this)가 SUCCESS이면 CANCEL 또는 REFUND로만 이동 가능


            // FAILED, CANCEL, REFUND 상태는 추가 전이 없음 (종료 상태)
            case FAILED:
            case CANCEL:
            case REFUND:
                return false;
        }

        // 모든 enum을 switch에서 처리했기 때문에 이 코드는 정상적으로는 도달하지 않음
        throw new IllegalStateException("Unknown status: " + this);
    }
}
