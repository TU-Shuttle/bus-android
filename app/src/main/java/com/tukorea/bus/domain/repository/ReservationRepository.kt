package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Reservation
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun getAllReservations(): Flow<List<Reservation>>
    suspend fun addReservation(reservation: Reservation): Long
    suspend fun deleteReservation(id: Long)
    fun getNextReservation(): Flow<Reservation?>
}

