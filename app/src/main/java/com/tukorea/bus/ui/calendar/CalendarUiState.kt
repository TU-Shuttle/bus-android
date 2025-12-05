package com.tukorea.bus.ui.calendar

import com.tukorea.bus.domain.model.Reservation

data class CalendarUiState(
    val reservations: List<Reservation> = emptyList(),
    val selectedDays: List<String> = emptyList(),
    val selectedTimes: List<String> = emptyList(),
    val scheduleType: String = "등교", // 등교/하교 타입 (모든 선택된 시간에 적용)
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

