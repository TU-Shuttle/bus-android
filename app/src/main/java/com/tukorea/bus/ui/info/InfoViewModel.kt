package com.tukorea.bus.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.ObserveTimetableUseCase
import com.tukorea.bus.ui.common.formatShortDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InfoUiState(
    val timetableTitle: String? = null,
    val period: String? = null,
    /** "11.6 04:00" */
    val lastChecked: String? = null,
)

@HiltViewModel
class InfoViewModel @Inject constructor(
    observeTimetable: ObserveTimetableUseCase,
) : ViewModel() {

    val uiState: StateFlow<InfoUiState> = observeTimetable()
        .map { timetable ->
            if (timetable == null) return@map InfoUiState()
            val from = formatShortDate(timetable.validFrom)
            val to = formatShortDate(timetable.validTo)
            InfoUiState(
                timetableTitle = timetable.title,
                period = if (from != null && to != null) "$from – $to" else null,
                lastChecked = timetable.fetchedAt?.let { fetched ->
                    val date = formatShortDate(fetched) ?: return@let null
                    "$date ${fetched.substringAfter('T', "").take(5)}".trim()
                },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InfoUiState())
}
