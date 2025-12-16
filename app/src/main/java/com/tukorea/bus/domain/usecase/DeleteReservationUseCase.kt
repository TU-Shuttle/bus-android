package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.ReservationRepository
import javax.inject.Inject

/**
 * 예약을 삭제하는 UseCase.
 */
class DeleteReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    /**
     * 지정된 ID의 예약을 삭제합니다.
     *
     * @param id 삭제할 예약의 ID
     */
    suspend operator fun invoke(id: Long) {
        repository.deleteReservation(id)
    }
}

