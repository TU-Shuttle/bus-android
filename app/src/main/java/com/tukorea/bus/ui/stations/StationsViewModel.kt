package com.tukorea.bus.ui.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetAllRoutesUseCase
import com.tukorea.bus.domain.usecase.GetStationsByRouteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationsViewModel @Inject constructor(
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val getStationsByRouteUseCase: GetStationsByRouteUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StationsUiState())
    val uiState: StateFlow<StationsUiState> = _uiState.asStateFlow()
    
    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getAllRoutesUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "노선 목록을 불러오는 중 오류가 발생했습니다."
                    )
                }
                .collect { routes ->
                    if (routes.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            routes = routes,
                            selectedRoute = routes.first(),
                            isLoading = false,
                            error = null
                        )
                        loadStations(routes.first())
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun selectRoute(route: String) {
        _uiState.value = _uiState.value.copy(selectedRoute = route)
        loadStations(route)
    }

    private fun loadStations(route: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getStationsByRouteUseCase(route)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "정류장 목록을 불러오는 중 오류가 발생했습니다."
                    )
                }
                .collect { stations ->
                    _uiState.value = _uiState.value.copy(
                        stations = stations,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}

