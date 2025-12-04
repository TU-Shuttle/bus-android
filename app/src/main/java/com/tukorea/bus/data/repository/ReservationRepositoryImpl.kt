package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 예약 Repository 구현체
 */
@Singleton
class ReservationRepositoryImpl @Inject constructor() : ReservationRepository {

    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    private val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    override fun getAllReservations(): Flow<List<Reservation>> {
        return reservations
    }

    override suspend fun addReservation(reservation: Reservation): Long {
        val newId = System.currentTimeMillis()
        val newReservation = reservation.copy(id = newId)
        _reservations.value += newReservation
        return newId
    }

    override suspend fun updateReservation(reservation: Reservation) {
        _reservations.value = _reservations.value.map {
            if (it.id == reservation.id) reservation else it
        }
    }

    override suspend fun deleteReservation(id: Long) {
        _reservations.value = _reservations.value.filter { it.id != id }
    }

    override fun getNextReservation(): Flow<Reservation?> {
        return reservations.map { it.firstOrNull() }
    }
}

