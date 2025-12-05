package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNextReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    operator fun invoke(): Flow<Reservation?> {
        return repository.getNextReservation()
    }
}

