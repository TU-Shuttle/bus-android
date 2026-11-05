package com.tukorea.bus.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.usecase.GetServiceMomentUseCase
import com.tukorea.bus.domain.usecase.ObserveMyRidesUseCase
import com.tukorea.bus.domain.usecase.ObserveServiceMomentUseCase
import com.tukorea.bus.domain.usecase.ObserveTimetableUseCase
import com.tukorea.bus.domain.usecase.RefreshTimetableUseCase
import com.tukorea.bus.domain.usecase.SuggestDirectionUseCase
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.formatShortDate
import com.tukorea.bus.ui.common.messageRes
import com.tukorea.bus.ui.ride.RideReservation
import com.tukorea.bus.ui.ride.RideSheetState
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
    observeMyRides: ObserveMyRidesUseCase,
    private val refreshTimetable: RefreshTimetableUseCase,
    private val getMoment: GetServiceMomentUseCase,
    private val suggestDirection: SuggestDirectionUseCase,
    private val rideReservation: RideReservation,
) : ViewModel() {

    private data class Local(
        val isRefreshing: Boolean = false,
        val refreshFailed: Boolean = false,
        val dayType: DayType? = null,
        val direction: ShuttleDirection? = null,
        val messageRes: Int? = null,
    )

    private val local = MutableStateFlow(Local())

    private val inputs = combine(observeTimetable(), observeMoment(), local) { timetable, moment, local ->
        Triple(timetable, moment, local)
    }

    val uiState: StateFlow<TimetableUiState> =
        combine(inputs, observeMyRides(), rideReservation.sheet) { (timetable, moment, local), rides, sheet ->
            buildState(timetable, moment, local, rides, sheet)
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

    fun openRide(departure: Departure) {
        val state = uiState.value
        if (!state.isToday || departure.isOnDemand) return
        rideReservation.open(RideTarget(getMoment().isoDate, departure.direction, departure.minute))
    }

    fun selectDropOff(stop: ShuttleStop) = rideReservation.select(stop)

    fun dismissRideSheet() = rideReservation.dismiss()

    fun confirmRide() {
        viewModelScope.launch {
            when (val result = rideReservation.submit() ?: return@launch) {
                is Result.Success -> local.update { it.copy(messageRes = R.string.ride_reserved_message) }
                is Result.Error -> local.update { it.copy(messageRes = result.error.messageRes()) }
            }
        }
    }

    fun consumeMessage() {
        local.update { it.copy(messageRes = null) }
    }

    private fun buildState(
        timetable: Timetable?,
        moment: ServiceMoment,
        local: Local,
        rides: List<RideIntent>,
        sheet: RideSheetState?,
    ): TimetableUiState {
        val dayType = local.dayType ?: moment.dayType ?: DayType.WEEKDAY
        if (timetable == null) {
            return TimetableUiState(
                isLoading = local.isRefreshing,
                loadFailed = !local.isRefreshing,
                selectedDayType = dayType,
                messageRes = local.messageRes,
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
        val reservedMinutes = rides
            .filter { it.serviceDate == moment.isoDate && it.direction == direction }
            .map { it.departMinute }
            .toSet()

        val hourRows = fixed.groupBy { it.minute / 60 }.map { (hour, departures) ->
            HourRow(
                hour = hour,
                cells = departures.map { dep ->
                    TimeCell(
                        departure = dep,
                        isPast = isToday && dep.minute < moment.minuteOfDay,
                        isNext = dep.minute == nextMinute,
                        isReserved = isToday && dep.minute in reservedMinutes,
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
            rideSheet = sheet,
            messageRes = local.messageRes,
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
