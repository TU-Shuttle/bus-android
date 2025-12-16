package com.tukorea.bus.ui.stations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetAllRoutesUseCase
import com.tukorea.bus.domain.usecase.GetStationsByRouteUseCase
import com.tukorea.bus.ui.common.ErrorMapper
import com.tukorea.bus.ui.common.collectWithUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 정류장 화면에서 노선 목록/선택된 노선의 정류장 목록을 관리하는 ViewModel.
 */
@HiltViewModel
class StationsViewModel @Inject constructor(
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val getStationsByRouteUseCase: GetStationsByRouteUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()

    private companion object {
        private const val TAG = "StationsViewModel"
    }
    
    init {
        Log.d(TAG, "초기화: 노선 목록을 불러옵니다.")
        loadRoutes()
    }

    /**
     * 전체 노선 목록을 불러와 상태를 갱신합니다.
     * - 로딩 중/성공/실패에 따른 로딩 플래그와 에러 메시지를 관리합니다.
     * - 성공 시 첫 번째 노선을 기본 선택 노선으로 설정합니다.
     */
    private fun loadRoutes() {
        viewModelScope.launch {
            getAllRoutesUseCase()
                .collectWithUiState(
                    state = _uiState,
                    onStart = { state ->
                        Log.d(TAG, "노선 목록 불러오기 시작")
                        state.copy(isLoading = true, error = null)
                    },
                    onError = { state, throwable ->
                        val message = errorMapper.run { throwable.toUserMessage() }
                        Log.w(TAG, "노선 목록 불러오기 실패: 메시지=$message", throwable)
                        state.copy(
                        isLoading = false,
                            error = message
                    )
                    },
                    onSuccess = { state, routes ->
                        Log.d(TAG, "노선 목록 불러오기 성공: 개수=${routes.size}")
                    if (routes.isNotEmpty()) {
                            state.copy(
                            routes = routes,
                            selectedRoute = routes.first(),
                            isLoading = false,
                            error = null
                        )
                    } else {
                            state.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
                )
        }
    }

    /**
     * 사용자가 노선을 선택했을 때 호출됩니다.
     *
     * @param route 선택한 노선 이름
     */
    fun selectRoute(route: String) {
        Log.d(TAG, "노선 선택: $route")
        _uiState.value = _uiState.value.copy(selectedRoute = route)
        loadStations(route)
    }

    /**
     * 주어진 노선에 대한 정류장 목록을 불러옵니다.
     *
     * @param route 정류장 목록을 조회할 노선 이름
     */
    private fun loadStations(route: String) {
        viewModelScope.launch {
            getStationsByRouteUseCase(route)
                .collectWithUiState(
                    state = _uiState,
                    onStart = { state ->
                        Log.d(TAG, "정류장 목록 불러오기 시작: 노선=$route")
                        state.copy(isLoading = true, error = null)
                    },
                    onError = { state, throwable ->
                        val message = errorMapper.run { throwable.toUserMessage() }
                        Log.w(TAG, "정류장 목록 불러오기 실패: 메시지=$message", throwable)
                        state.copy(
                        isLoading = false,
                            error = message
                    )
                    },
                    onSuccess = { state, stations ->
                        Log.d(TAG, "정류장 목록 불러오기 성공: 개수=${stations.size}")
                        state.copy(
                        stations = stations,
                        isLoading = false,
                        error = null
                    )
                }
                )
        }
    }
}
