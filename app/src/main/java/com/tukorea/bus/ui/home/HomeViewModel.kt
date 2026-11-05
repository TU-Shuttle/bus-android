package com.tukorea.bus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideSummary
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.usecase.CancelRideUseCase
import com.tukorea.bus.domain.usecase.GetRideSummaryUseCase
import com.tukorea.bus.domain.usecase.GetServiceMomentUseCase
import com.tukorea.bus.domain.usecase.GetUpcomingDeparturesUseCase
import com.tukorea.bus.domain.usecase.ObserveLiveBusesUseCase
import com.tukorea.bus.domain.usecase.ObserveMyRidesUseCase
import com.tukorea.bus.domain.usecase.ObserveServiceMomentUseCase
import com.tukorea.bus.domain.usecase.ObserveTimetableUseCase
import com.tukorea.bus.domain.usecase.RefreshMyRidesUseCase
import com.tukorea.bus.domain.usecase.RefreshTimetableUseCase
import com.tukorea.bus.domain.usecase.SuggestDirectionUseCase
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.formatShortDate
import com.tukorea.bus.ui.common.messageRes
import com.tukorea.bus.ui.ride.RideReservation
import com.tukorea.bus.ui.ride.RideSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTimetable: ObserveTimetableUseCase,
    observeMoment: ObserveServiceMomentUseCase,
    observeLiveBuses: ObserveLiveBusesUseCase,
    observeMyRides: ObserveMyRidesUseCase,
    private val refreshTimetable: RefreshTimetableUseCase,
    private val refreshMyRides: RefreshMyRidesUseCase,
    private val getRideSummary: GetRideSummaryUseCase,
    private val cancelRide: CancelRideUseCase,
    private val getMoment: GetServiceMomentUseCase,
    private val getUpcoming: GetUpcomingDeparturesUseCase,
    private val suggestDirection: SuggestDirectionUseCase,
    private val rideReservation: RideReservation,
) : ViewModel() {

    private data class Local(
        val isRefreshing: Boolean = true,
        val refreshFailed: Boolean = false,
        val selected: ShuttleDirection? = null,
        val summaries: List<RideSummary> = emptyList(),
        val messageRes: Int? = null,
    )

    private val local = MutableStateFlow(Local())

    private val liveBuses: StateFlow<AppResult<List<LiveBus>>?> =
        observeLiveBuses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    private val base = combine(observeTimetable(), observeMoment(), local) { timetable, moment, local ->
        Triple(timetable, moment, local)
    }

    val uiState: StateFlow<HomeUiState> =
        combine(base, liveBuses, observeMyRides(), rideReservation.sheet) { (timetable, moment, local), live, rides, sheet ->
            buildState(timetable, moment, local, live, rides, sheet)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    init {
        refresh()
        viewModelScope.launch { refreshMyRides() }
        viewModelScope.launch {
            while (true) {
                loadSummary()
                delay(SUMMARY_INTERVAL_MS)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            local.update { it.copy(isRefreshing = true) }
            val result = refreshTimetable()
            local.update { it.copy(isRefreshing = false, refreshFailed = result is Result.Error) }
        }
    }

    fun selectDirection(direction: ShuttleDirection) {
        local.update { it.copy(selected = direction) }
    }

    fun reserveNext() {
        val state = uiState.value
        val direction = state.selectedDirection ?: return
        val departure = state.nextDeparture ?: return
        rideReservation.open(RideTarget(getMoment().isoDate, direction, departure.minute))
    }

    fun selectDropOff(stop: ShuttleStop) = rideReservation.select(stop)

    fun dismissRideSheet() = rideReservation.dismiss()

    fun confirmRide() {
        viewModelScope.launch {
            when (val result = rideReservation.submit() ?: return@launch) {
                is Result.Success -> {
                    showMessage(R.string.ride_reserved_message)
                    loadSummary()
                }
                is Result.Error -> showMessage(result.error.messageRes())
            }
        }
    }

    fun cancelReservedNext() {
        val ride = uiState.value.reservedForNext ?: return
        viewModelScope.launch {
            when (val result = cancelRide(ride.id)) {
                is Result.Success -> {
                    showMessage(R.string.ride_cancelled_message)
                    loadSummary()
                }
                is Result.Error -> showMessage(result.error.messageRes())
            }
        }
    }

    fun consumeMessage() {
        local.update { it.copy(messageRes = null) }
    }

    private fun showMessage(res: Int) {
        local.update { it.copy(messageRes = res) }
    }

    private suspend fun loadSummary() {
        val result = getRideSummary(getMoment().isoDate)
        if (result is Result.Success) local.update { it.copy(summaries = result.data) }
    }

    private fun buildState(
        timetable: Timetable?,
        moment: ServiceMoment,
        local: Local,
        live: AppResult<List<LiveBus>>?,
        rides: List<RideIntent>,
        sheet: RideSheetState?,
    ): HomeUiState {
        val liveList = (live as? Result.Success)?.data.orEmpty()
        val liveFailed = live is Result.Error
        if (timetable == null) {
            return HomeUiState(
                isLoading = local.isRefreshing,
                loadFailed = !local.isRefreshing && local.refreshFailed,
                liveBuses = liveList,
                liveUnavailable = liveFailed,
                messageRes = local.messageRes,
            )
        }

        val directions = moment.dayType?.let(timetable::directionsOn).orEmpty()
        val selected = local.selected?.takeIf { it in directions } ?: suggestDirection(timetable, moment)
        val upcoming = selected?.let { getUpcoming(timetable, it, moment) }
        val next = (upcoming as? com.tukorea.bus.domain.model.UpcomingDepartures.Scheduled)?.next?.firstOrNull()

        return HomeUiState(
            isLoading = false,
            directions = directions,
            selectedDirection = selected,
            upcoming = upcoming,
            nowMinute = moment.minuteOfDay,
            outOfPeriod = if (timetable.isValidOn(moment.isoDate)) null else periodLabel(timetable),
            liveBuses = liveList,
            liveUnavailable = liveFailed,
            nextRiders = next?.let { dep ->
                local.summaries.firstOrNull { it.direction == selected && it.departMinute == dep.minute }?.riders
            } ?: 0,
            reservedForNext = next?.let { dep ->
                rides.firstOrNull {
                    it.serviceDate == moment.isoDate && it.direction == selected && it.departMinute == dep.minute
                }
            },
            rideSheet = sheet,
            messageRes = local.messageRes,
        )
    }

    private fun periodLabel(timetable: Timetable): String? {
        val from = formatShortDate(timetable.validFrom) ?: return null
        val to = formatShortDate(timetable.validTo) ?: return null
        return "$from – $to"
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val SUMMARY_INTERVAL_MS = 60_000L
    }
}
