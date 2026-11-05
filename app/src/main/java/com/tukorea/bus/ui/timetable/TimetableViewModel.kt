package com.tukorea.bus.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.usecase.ObserveServiceMomentUseCase
import com.tukorea.bus.domain.usecase.ObserveTimetableUseCase
import com.tukorea.bus.domain.usecase.RefreshTimetableUseCase
import com.tukorea.bus.domain.usecase.SuggestDirectionUseCase
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.formatShortDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    observeTimetable: ObserveTimetableUseCase,
    observeMoment: ObserveServiceMomentUseCase,
    private val refreshTimetable: RefreshTimetableUseCase,
    private val suggestDirection: SuggestDirectionUseCase,
) : ViewModel() {

    private data class Local(
        val isRefreshing: Boolean = false,
        val refreshFailed: Boolean = false,
        val dayType: DayType? = null,
        val direction: ShuttleDirection? = null,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<TimetableUiState> =
        combine(observeTimetable(), observeMoment(), local) { timetable, moment, local ->
            buildState(timetable, moment, local)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimetableUiState())

    fun selectDayType(dayType: DayType) {
        local.update { it.copy(dayType = dayType, direction = null) }
    }

    fun selectDirection(direction: ShuttleDirection) {
        local.update { it.copy(direction = direction) }
    }

    fun retry() {
        viewModelScope.launch {
            local.update { it.copy(isRefreshing = true) }
            val result = refreshTimetable()
            local.update { it.copy(isRefreshing = false, refreshFailed = result is Result.Error) }
        }
    }

    private fun buildState(
        timetable: Timetable?,
        moment: ServiceMoment,
        local: Local,
    ): TimetableUiState {
        val dayType = local.dayType ?: moment.dayType ?: DayType.WEEKDAY
        if (timetable == null) {
            return TimetableUiState(
                isLoading = local.isRefreshing,
                loadFailed = !local.isRefreshing,
                selectedDayType = dayType,
            )
        }

        val directions = timetable.directionsOn(dayType)
        val direction = local.direction?.takeIf { it in directions }
            ?: suggestDirection(timetable, moment)?.takeIf { it in directions }
            ?: directions.firstOrNull()
        val isToday = moment.dayType == dayType
        val rows = timetable.departures
            .filter { it.dayType == dayType && it.direction == direction }
            .sortedBy { it.minute }

        val fixed = rows.filterNot { it.isOnDemand }
        val nextMinute = if (isToday) fixed.firstOrNull { it.minute >= moment.minuteOfDay }?.minute else null

        val hourRows = fixed.groupBy { it.minute / 60 }.map { (hour, departures) ->
            HourRow(
                hour = hour,
                cells = departures.map { dep ->
                    TimeCell(
                        departure = dep,
                        isPast = isToday && dep.minute < moment.minuteOfDay,
                        isNext = dep.minute == nextMinute,
                    )
                },
            )
        }

        return TimetableUiState(
            isLoading = false,
            title = timetable.title,
            period = periodOf(timetable),
            selectedDayType = dayType,
            directions = directions,
            selectedDirection = direction,
            onDemand = rows.filter { it.isOnDemand },
            hours = hourRows,
            isCompact = hourRows.isNotEmpty() && hourRows.all { it.cells.size <= COMPACT_MAX_PER_HOUR },
            notes = fixed.filter { it.note != null },
            isToday = isToday,
        )
    }

    private fun periodOf(timetable: Timetable): String? {
        val from = formatShortDate(timetable.validFrom) ?: return null
        val to = formatShortDate(timetable.validTo) ?: return null
        return "$from – $to"
    }

    private companion object {
        const val COMPACT_MAX_PER_HOUR = 2
    }
}
