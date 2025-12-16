package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.error.ValidationError
import javax.inject.Inject

/**
 * 예약 정보의 유효성을 검증하는 UseCase.
 */
class ValidateReservationUseCase @Inject constructor() {
    /**
     * 예약 정보의 유효성을 검증합니다.
     *
     * @param selectedFrom 출발지
     * @param selectedTo 도착지
     * @param selectedDays 선택된 요일 목록
     * @param selectedTimes 선택된 시간 목록
     * @return 검증 실패 시 ValidationError, 성공 시 null
     */
    operator fun invoke(
        selectedFrom: String,
        selectedTo: String,
        selectedDays: List<String>,
        selectedTimes: List<String>
    ): ValidationError? = when {
        selectedFrom == selectedTo -> ValidationError.SameRoute
        selectedDays.isEmpty() -> ValidationError.SelectDay
        selectedTimes.isEmpty() -> ValidationError.SelectTime
        else -> null
    }
}

