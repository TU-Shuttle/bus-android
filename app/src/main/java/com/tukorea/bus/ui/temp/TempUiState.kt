package com.tukorea.bus.ui.temp

import com.tukorea.bus.domain.model.Temp

data class TempUiState(
    val isLoading: Boolean = false,
    val temps: List<Temp> = emptyList(),
    val error: String? = null
)