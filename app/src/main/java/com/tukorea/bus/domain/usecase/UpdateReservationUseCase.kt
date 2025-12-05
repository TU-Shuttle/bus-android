package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import javax.inject.Inject

class UpdateReservationUseCase @Inject constructor(
    private val repository: ReservationRepository
) {
    suspend operator fun invoke(reservation: Reservation) {
        repository.updateReservation(reservation)
    }
}
