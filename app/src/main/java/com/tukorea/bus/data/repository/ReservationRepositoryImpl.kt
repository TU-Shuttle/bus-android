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

@Singleton
class ReservationRepositoryImpl @Inject constructor(
) : ReservationRepository {
    
    private val _reservations = MutableStateFlow(
        listOf(
            Reservation(
                id = 1,
                days = listOf("월", "수", "금"),
                time = "09:00",
                from = "기숙사",
                to = "본관"
            ),
            Reservation(
                id = 2,
                days = listOf("화", "목"),
                time = "13:30",
                from = "본관",
                to = "도서관"
            ),
            Reservation(
                id = 3,
                days = listOf("월", "화", "수", "목", "금"),
                time = "17:00",
                from = "역",
                to = "기숙사"
            )
        )
    )
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
    
    override suspend fun deleteReservation(id: Long) {
        _reservations.value = _reservations.value.filter { it.id != id }
    }

    override fun getNextReservation(): Flow<Reservation?> {
        return reservations.map { it.firstOrNull() }
    }
}

