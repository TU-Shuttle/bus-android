package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 다음 예약 정보를 가져오는 UseCase.
 */
class GetNextReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    /**
     * 가장 가까운 다음 예약 정보를 반환합니다.
     *
     * @return 다음 예약 정보를 포함한 Flow (예약이 없으면 null)
     */
    operator fun invoke(): Flow<Reservation?> {
        return repository.getNextReservation()
    }
}

