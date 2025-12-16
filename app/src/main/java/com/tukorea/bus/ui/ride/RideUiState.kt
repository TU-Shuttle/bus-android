package com.tukorea.bus.ui.ride

import com.tukorea.bus.ui.home.ModalHeight
data class RideUiState(
    val remainingMinutes: Int = 0,
    val busStatus: String = "",
    val departureStation: String = "",
    val departureTime: String = "",
    val arrivalStation: String = "",
    val arrivalTime: String = "",
    val modalHeight: ModalHeight = ModalHeight.MID,
    val isLoading: Boolean = false,
    val error: String? = null
)

