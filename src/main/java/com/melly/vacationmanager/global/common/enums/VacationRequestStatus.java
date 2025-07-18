package com.melly.vacationmanager.global.common.enums;

public enum VacationRequestStatus {
    /** 사용자가 신청, 승인 대기 중 */
    PENDING,

    /** 관리자가 승인하여 휴가 확정 */
    APPROVED,

    /** 관리자 보류, 보완 후 재신청 가능 */
    ON_HOLD,

    /** 관리자 반려, 종료 상태 */
    REJECTED,

    /** 사용자가 직접 취소 */
    CANCELLED
}
