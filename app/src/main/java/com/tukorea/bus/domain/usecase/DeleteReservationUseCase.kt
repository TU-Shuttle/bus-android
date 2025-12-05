package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.ReservationRepository
import javax.inject.Inject

class DeleteReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteReservation(id)
    }
}

