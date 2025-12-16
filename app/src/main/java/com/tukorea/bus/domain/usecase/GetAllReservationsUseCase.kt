package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 모든 예약 목록을 가져오는 UseCase.
 */
class GetAllReservationsUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    /**
     * 저장된 모든 예약 목록을 반환합니다.
     *
     * @return 예약 목록을 포함한 Flow
     */
    operator fun invoke(): Flow<List<Reservation>> {
        return repository.getAllReservations()
    }
}

