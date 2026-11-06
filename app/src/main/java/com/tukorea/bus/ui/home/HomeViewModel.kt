package com.tukorea.bus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.repository.RouteRepository
import com.tukorea.bus.domain.usecase.TrackBusUseCase
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.usecase.GetUpcomingDeparturesUseCase
import com.tukorea.bus.domain.usecase.ObserveLiveBusesUseCase
import com.tukorea.bus.domain.usecase.ObserveServiceMomentUseCase
import com.tukorea.bus.domain.usecase.ObserveTimetableUseCase
import com.tukorea.bus.domain.usecase.RefreshTimetableUseCase
import com.tukorea.bus.domain.usecase.SuggestDirectionUseCase
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.formatShortDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTimetable: ObserveTimetableUseCase,
    observeMoment: ObserveServiceMomentUseCase,
    observeLiveBuses: ObserveLiveBusesUseCase,
    private val refreshTimetable: RefreshTimetableUseCase,
    private val getUpcoming: GetUpcomingDeparturesUseCase,
    private val suggestDirection: SuggestDirectionUseCase,
    private val routes: RouteRepository,
    private val trackBus: TrackBusUseCase,
) : ViewModel() {

    private data class Local(
        val isRefreshing: Boolean = true,
        val refreshFailed: Boolean = false,
        val selected: ShuttleDirection? = null,
        val selectedBusId: String? = null,
    )

    /** 버스별 지금 위치와 그 직전 위치(방향·속도 계산용). */
    private data class LiveHistory(
        val result: AppResult<List<LiveBus>>? = null,
        val previous: Map<String, LiveBus> = emptyMap(),
    )

    private val local = MutableStateFlow(Local())

    private val liveBuses: StateFlow<LiveHistory> =
        observeLiveBuses()
            .scan(LiveHistory()) { acc, result ->
                val before = (acc.result as? Result.Success)?.data.orEmpty().associateBy { it.id }
                val now = (result as? Result.Success)?.data ?: return@scan acc.copy(result = result)
                // 새 위치가 왔을 때만 직전 위치를 바꾼다(같은 위치를 다시 받으면 유지).
                val previous = now.mapNotNull { bus ->
                    val last = before[bus.id]
                    val prev = if (last != null && last.updatedAtMillis != bus.updatedAtMillis) last else acc.previous[bus.id]
                    prev?.let { bus.id to it }
                }.toMap()
                LiveHistory(result, previous)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LiveHistory())

    val uiState: StateFlow<HomeUiState> =
        combine(observeTimetable(), observeMoment(), local, liveBuses) { timetable, moment, local, live ->
            buildState(timetable, moment, local, live)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    init {
        refresh()
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

    fun selectBus(id: String) {
        local.update { it.copy(selectedBusId = id) }
    }

    fun clearSelectedBus() {
        local.update { it.copy(selectedBusId = null) }
    }

    private fun buildState(
        timetable: Timetable?,
        moment: ServiceMoment,
        local: Local,
        history: LiveHistory,
    ): HomeUiState {
        val live = history.result
        val liveList = (live as? Result.Success)?.data.orEmpty()
        val liveFailed = live is Result.Error
        if (timetable == null) {
            return HomeUiState(
                isLoading = local.isRefreshing,
                loadFailed = !local.isRefreshing && local.refreshFailed,
                liveBuses = liveList,
                liveUnavailable = liveFailed,
            )
        }

        val directions = moment.dayType?.let { timetable.directionsOn(it, moment.isoDayOfWeek) }.orEmpty()
        val selected = local.selected?.takeIf { it in directions } ?: suggestDirection(timetable, moment)
        val route = selected?.let(routes::routeOf)
        val tracking = liveList.associate { bus -> bus.id to trackBus(bus, history.previous[bus.id], route) }
        return HomeUiState(
            isLoading = false,
            directions = directions,
            selectedDirection = selected,
            upcoming = selected?.let { getUpcoming(timetable, it, moment) },
            nowMinute = moment.minuteOfDay,
            outOfPeriod = if (timetable.isValidOn(moment.isoDate)) null else periodLabel(timetable),
            liveBuses = liveList,
            liveUnavailable = liveFailed,
            route = route,
            headings = tracking.mapNotNull { (id, t) -> t.headingDegrees?.let { id to it } }.toMap(),
            selectedBus = local.selectedBusId?.let(tracking::get),
        )
    }

    private fun periodLabel(timetable: Timetable): String? {
        val from = formatShortDate(timetable.validFrom) ?: return null
        val to = formatShortDate(timetable.validTo) ?: return null
        return "$from – $to"
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
