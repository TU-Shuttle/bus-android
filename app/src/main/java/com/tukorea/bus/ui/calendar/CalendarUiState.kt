package com.tukorea.bus.ui.calendar

import com.tukorea.bus.domain.model.Reservation

data class CalendarUiState(
    val reservations: List<Reservation> = emptyList(),
    val selectedDays: List<String> = emptyList(),
    val selectedTime: String = "",
    val selectedFrom: String = "",
    val selectedTo: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val days: List<String> = emptyList(),
    val times: List<String> = emptyList(),
    val locations: List<String> = emptyList()
)

