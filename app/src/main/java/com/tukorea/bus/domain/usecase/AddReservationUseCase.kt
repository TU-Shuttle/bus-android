package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import javax.inject.Inject

/**
 * 새로운 예약을 추가하는 UseCase.
 */
class AddReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    /**
     * 새로운 예약을 추가합니다.
     *
     * @param reservation 추가할 예약 정보
     * @return 생성된 예약의 ID
     */
    suspend operator fun invoke(reservation: Reservation): Long {
        return repository.addReservation(reservation)
    }
}

