package com.tukorea.bus.ui.realtime

data class RealtimeUiState(
    val remainingMinutes: Int = 0,
    val progress: Float = 0f,
    val remainingStops: Int = 0,
    val route: String = "",
    val from: String = "",
    val to: String = "",
    val stations: List<com.tukorea.bus.domain.model.Station> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

