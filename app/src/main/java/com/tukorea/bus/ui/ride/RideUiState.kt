package com.tukorea.bus.ui.ride

data class RideUiState(
    val remainingMinutes: Int = 0,
    val busStatus: String = "",
    val departureStation: String = "",
    val departureTime: String = "",
    val arrivalStation: String = "",
    val arrivalTime: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

