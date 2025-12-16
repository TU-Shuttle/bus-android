package com.tukorea.bus.ui.quickride

import com.tukorea.bus.domain.model.Bus
import com.tukorea.bus.domain.model.RouteScheduleTemplate

data class QuickRideUiState(
    val currentLocation: String = "",
    val selectedDestination: String = "",
    val destinations: List<String> = emptyList(),
    val availableBuses: List<Bus> = emptyList(),
    val timetableRoutes: List<RouteScheduleTemplate> = emptyList(),
    val showTimeTable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

