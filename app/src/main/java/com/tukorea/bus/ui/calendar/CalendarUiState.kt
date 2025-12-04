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
    val locations: List<String> = emptyList(),
    val editingReservationId: Long? = null,  // 수정 중인 예약 ID
    val deletingReservationId: Long? = null  // 삭제 확인 다이얼로그에 표시할 예약 ID
)

