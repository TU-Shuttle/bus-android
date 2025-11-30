package com.tukorea.bus.ui.stations

import com.tukorea.bus.domain.model.Station

data class StationsUiState(
    val routes: List<String> = emptyList(),
    val selectedRoute: String = "",
    val stations: List<Station> = emptyList(),
    val interval: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

