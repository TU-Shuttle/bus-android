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
        val showPast: Boolean = false,
    )

    private val local = MutableStateFlow(Local())

    val uiState: StateFlow<TimetableUiState> =
        combine(observeTimetable(), observeMoment(), local) { timetable, moment, local ->
            buildState(timetable, moment, local)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimetableUiState())

    fun selectDayType(dayType: DayType) {
        local.update { it.copy(dayType = dayType, direction = null, showPast = false) }
    }

    fun togglePast() {
        local.update { it.copy(showPast = !it.showPast) }
    }

    fun selectDirection(direction: ShuttleDirection) {
        local.update { it.copy(direction = direction, showPast = false) }
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

        val now = moment.minuteOfDay
        val nextMinute = if (isToday) {
            rows.firstOrNull { (it.endMinute ?: it.minute) >= now }?.minute
        } else {
            null
        }
        val all = rows.map { dep ->
            val isPast = isToday && (dep.endMinute ?: dep.minute) < now
            DepartureRow(
                departure = dep,
                isPast = isPast,
                isNext = dep.minute == nextMinute,
                isRunningNow = isToday && dep.isOnDemand && dep.minute <= now && !isPast,
                minutesUntil = if (isToday && !isPast) (dep.minute - now).coerceAtLeast(0) else null,
            )
        }

        return TimetableUiState(
            isLoading = false,
            title = timetable.title,
            period = periodOf(timetable),
            selectedDayType = dayType,
            directions = directions,
            selectedDirection = direction,
            past = all.filter { it.isPast },
            showPast = local.showPast,
            upcoming = all.filterNot { it.isPast },
        )
    }

    private fun periodOf(timetable: Timetable): String? {
        val from = formatShortDate(timetable.validFrom) ?: return null
        val to = formatShortDate(timetable.validTo) ?: return null
        return "$from – $to"
    }
}
