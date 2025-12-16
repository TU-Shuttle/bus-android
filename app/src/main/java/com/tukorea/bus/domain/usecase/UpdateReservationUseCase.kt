package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import javax.inject.Inject

/**
 * 기존 예약을 수정하는 UseCase.
 */
class UpdateReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    /**
     * 기존 예약 정보를 업데이트합니다.
     *
     * @param reservation 수정할 예약 정보
     */
    suspend operator fun invoke(reservation: Reservation) {
        repository.updateReservation(reservation)
    }
}
