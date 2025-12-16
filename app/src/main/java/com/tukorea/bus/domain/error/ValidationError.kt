package com.tukorea.bus.domain.error

/**
 * 예약 검증 에러 타입
 * Domain 레이어에서 검증 실패를 정의합니다.
 */
sealed class ValidationError {
    object SameRoute : ValidationError()
    object SelectDay : ValidationError()
    object SelectTime : ValidationError()
    object SelectDayFirst : ValidationError()
}

