package com.tukorea.bus.ui.quickride

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.error.MapError
import com.tukorea.bus.domain.usecase.GetAvailableBusesUseCase
import com.tukorea.bus.domain.usecase.GetDefaultLocationsUseCase
import com.tukorea.bus.domain.usecase.GetLocationsUseCase
import com.tukorea.bus.domain.usecase.GetNearestBusStopUseCase
import com.tukorea.bus.domain.usecase.NearestBusStopResult
import com.tukorea.bus.domain.usecase.GetRouteSchedulesUseCase
import com.tukorea.bus.domain.util.DistanceCalculator
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * QuickRide 화면의 현재 위치, 목적지, 이용 가능 버스, 시간표 데이터를 관리하는 ViewModel.
 */
@HiltViewModel
class QuickRideViewModel @Inject constructor(
    private val getAvailableBusesUseCase: GetAvailableBusesUseCase,
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getRouteSchedulesUseCase: GetRouteSchedulesUseCase,
    private val getDefaultLocationsUseCase: GetDefaultLocationsUseCase,
    private val getNearestBusStopUseCase: GetNearestBusStopUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickRideUiState())
    val uiState: StateFlow<QuickRideUiState> = _uiState.asStateFlow()

    private companion object {
        private const val TAG = "QuickRideViewModel"
    }

    init {
        Log.d(TAG, "초기화: 목적지와 시간표 노선을 불러옵니다.")
        loadDestinations()
        loadTimetableRoutes()
    }
    
    /**
     * 캘린더 저장소에서 목적지 목록을 불러와 UI 상태를 갱신합니다.
     *
     * - 목적지 목록이 존재하지만 현재 위치/목적지가 비어 있으면 기본값을 자동 선택합니다.
     * - 로딩 실패 시에는 에러 메시지만 로그로 남기고 화면에는 별도 에러를 표시하지 않습니다.
     */
    private fun loadDestinations() {
        viewModelScope.launch {
            getLocationsUseCase()
                .catch { throwable ->
                    // 목적지 목록 로딩 실패 시, 초기 화면만 비워두고 별도 에러는 표시하지 않는다.
                    Log.w(TAG, "목적지 목록 불러오기 실패", throwable)
                }
                .collect { locations ->
                    val currentState = _uiState.value
                    val defaultLocations = getDefaultLocationsUseCase(
                        locations = locations,
                        currentLocation = currentState.currentLocation,
                        currentDestination = currentState.selectedDestination
                    )
                    // 현재 상태를 기준으로 목적지/현재 위치/선택된 목적지를 계산해 새 상태를 만든다.
                    val newState = currentState.copy(
                        destinations = locations,
                        currentLocation = defaultLocations.from,
                        selectedDestination = defaultLocations.to
                    )
                    Log.d(
                        TAG,
                        "목적지 불러오기 완료: 개수=${locations.size}, 현재=${newState.currentLocation}, 목적지=${newState.selectedDestination}"
                    )
                    _uiState.value = newState

                    // 초기 진입 시 기본 출발/도착지가 모두 설정되면
                    // 목적지 기준으로 가장 가까운 정류장을 계산한 뒤 이용 가능 버스를 불러온다.
                    if (newState.selectedDestination.isNotEmpty()) {
                        loadNearestBusStop()
                    }
                }
        }
    }

    /**
     * 시간표 화면에서 사용할 노선 시간표 목록을 불러옵니다.
     */
    private fun loadTimetableRoutes() {
        viewModelScope.launch {
            val routes = getRouteSchedulesUseCase()
            Log.d(TAG, "시간표 노선 불러오기 완료: 개수=${routes.size}")
            _uiState.value = _uiState.value.copy(timetableRoutes = routes)
        }
    }

    /**
     * 선택된 목적지를 변경하고, 변경된 목적지 기준으로 이용 가능 버스 목록을 다시 불러옵니다.
     *
     * @param destination 사용자가 새로 선택한 목적지 이름
     */
    fun updateDestination(destination: String) {
        Log.d(TAG, "목적지 선택 변경: $destination")
        _uiState.value = _uiState.value.copy(selectedDestination = destination)
        // 목적지 변경 시, 해당 목적지로 갈 수 있는 가장 가까운 정류장을 다시 계산
        // 이후 최신 출발지/목적지 조합으로 버스 목록 갱신
        loadNearestBusStop()
    }

    /**
     * 시간표 펼침 여부를 토글합니다.
     * - `true`면 시간표를 표시하고, `false`면 숨깁니다.
     */
    fun toggleTimeTable() {
        val newValue = !_uiState.value.showTimeTable
        Log.d(TAG, "시간표 펼침 여부 변경: $newValue")
        _uiState.value = _uiState.value.copy(showTimeTable = newValue)
    }

    /**
     * 현재 출발지/목적지 조합으로 이용 가능한 버스 목록을 불러옵니다.
     * - 호출 시 로딩 상태로 전환하고, 성공/실패에 따라 상태를 업데이트합니다.
     */
    private fun loadAvailableBuses() {
        viewModelScope.launch {
        val state = _uiState.value
            Log.d(
                TAG,
                "이용 가능 버스 조회 시작: 출발=${state.currentLocation}, 도착=${state.selectedDestination}"
            )
            _uiState.value = state.copy(isLoading = true, error = null)

            getAvailableBusesUseCase(state.currentLocation, state.selectedDestination)
                .catch { exception ->
                    val message = errorMapper.run { exception.toUserMessage() }
                    Log.w(TAG, "이용 가능 버스 조회 실패: 메시지=$message", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = message
                    )
                }
                .collect { buses ->
                    Log.d(TAG, "이용 가능 버스 조회 성공: 개수=${buses.size}")
        _uiState.value = _uiState.value.copy(
                        availableBuses = buses,
                        isLoading = false,
                        error = null
        )
                }
        }
    }

    /**
     * 현재 위치에서 가장 가까운 정류장을 찾아 UI 상태를 업데이트합니다.
     * 하버사인 공식을 사용하여 거리를 계산하고, 가장 가까운 정류장의 이름과 거리를 표시합니다.
     */
    private fun loadNearestBusStop() {
        viewModelScope.launch {
            val destination = _uiState.value.selectedDestination
            if (destination.isEmpty()) {
                Log.d(TAG, "가장 가까운 정류장 찾기 건너뜀: 목적지 미선택")
                return@launch
            }

            Log.d(TAG, "가장 가까운 정류장 찾기 시작 (목적지=$destination)")
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)

            when (val result = getNearestBusStopUseCase(destination)) {
                is Result.Success<*> -> {
                    val nearestStop = result.data as NearestBusStopResult
                    val distanceText = DistanceCalculator.formatDistance(nearestStop.distance)
                    Log.d(
                        TAG,
                        "가장 가까운 정류장: ${nearestStop.busStop.name}, 거리: $distanceText"
                    )
                    _uiState.value = _uiState.value.copy(
                        currentLocation = nearestStop.busStop.name,
                        nearestStopDistance = distanceText,
                        isLoadingLocation = false
                    )

                    // 가장 가까운 정류장을 찾은 후 이용 가능한 버스 목록 로드
                    loadAvailableBuses()
                }
                is Result.Error<*> -> {
                    val message = errorMapper.run { (result.error as MapError).toUserMessage() }
                    Log.w(TAG, "가장 가까운 정류장 찾기 실패: $message")
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false,
                        error = message
                    )
                }
            }
        }
    }

    /**
     * 수동으로 가장 가까운 정류장을 다시 찾습니다.
     * UI에서 새로고침 버튼 등을 통해 호출할 수 있습니다.
     */
    fun refreshNearestBusStop() {
        loadNearestBusStop()
    }
}
