package com.tukorea.bus.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetCurrentLocationUseCase
import com.tukorea.bus.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())

    val state: StateFlow<MapUiState> = _state

    init {
        checkLocationPermission()
    }

    /**
     * 위치 권한이 허용되었는지 확인하고 상태를 업데이트합니다.
     */
    fun checkLocationPermission() {
        val isGranted = getCurrentLocationUseCase.isLocationPermissionGranted()
        _state.value = _state.value.copy(isLocationPermissionGranted = isGranted)
    }

    /**
     * 현재 위치를 로드합니다.
     * UseCase를 통해 위치 정보를 가져와 상태를 업데이트합니다.
     * Domain 레이어에서 반환된 Result를 UI 상태로 변환합니다.
     */
    fun loadCurrentLocation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = getCurrentLocationUseCase()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false, currentLocation = result.data, error = null
                    )
                }

                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false, error = result.error.getMessage()
                    )
                }
            }
        }
    }

    /**
     * 위치 권한이 허용되었을 때 호출됩니다.
     * 권한 상태를 업데이트하고 현재 위치를 로드합니다.
     */
    fun onLocationPermissionGranted() {
        _state.value = _state.value.copy(isLocationPermissionGranted = true)
        loadCurrentLocation()
    }
}

