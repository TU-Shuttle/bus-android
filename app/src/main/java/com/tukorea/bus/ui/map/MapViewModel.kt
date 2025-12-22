package com.tukorea.bus.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.repository.BusStopBusInfo
import com.tukorea.bus.domain.usecase.GetAllBusStopsUseCase
import com.tukorea.bus.domain.usecase.GetBusesForStopUseCase
import com.tukorea.bus.domain.usecase.GetCurrentLocationUseCase
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 지도 화면의 현재 위치 상태를 관리하는 ViewModel.
 *
 * - 위치 권한 여부
 * - 현재 단말 위치
 * - 로딩/에러 상태
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getAllBusStopsUseCase: GetAllBusStopsUseCase,
    private val getBusesForStopUseCase: GetBusesForStopUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state

    private companion object {
        private const val TAG = "MapViewModel"
    }

    init {
        Log.d(TAG, "초기화: 위치 권한을 확인합니다.")
        checkLocationPermission()
        loadBusStops()
    }
    
    /**
     * 정류장 목록을 로드합니다.
     * UseCase를 통해 정류장 정보를 가져와 상태를 업데이트합니다.
     */
    private fun loadBusStops() {
        Log.d(TAG, "정류장 목록 불러오기 시작")
        viewModelScope.launch {
            try {
                val busStops = getAllBusStopsUseCase()
                Log.d(TAG, "정류장 목록 불러오기 성공: ${busStops.size}개")
                _state.value = _state.value.copy(busStops = busStops)
            } catch (e: Exception) {
                Log.e(TAG, "정류장 목록 불러오기 실패", e)
                // 정류장 로드 실패는 치명적이지 않으므로 에러를 표시하지 않음
            }
        }
    }

    /**
     * 위치 권한이 허용되었는지 확인합니다.
     * - 권한 여부만 조회하며, 실제 위치 조회는 수행하지 않습니다.
     */
    fun checkLocationPermission() {
        val isGranted = getCurrentLocationUseCase.isLocationPermissionGranted()
        Log.d(TAG, "위치 권한 여부 확인: 허용 여부=$isGranted")
        _state.value = _state.value.copy(isLocationPermissionGranted = isGranted)
    }

    /**
     * 현재 위치를 로드합니다.
     * UseCase를 통해 위치 정보를 가져와 상태를 업데이트합니다.
     */
    fun loadCurrentLocation() {
        Log.d(TAG, "현재 위치 불러오기 시작")
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = getCurrentLocationUseCase()) {
                is Result.Success -> {
                    Log.d(
                        TAG,
                        "현재 위치 불러오기 성공: 위도=${result.data.latitude}, 경도=${result.data.longitude}"
                    )
                    _state.value = _state.value.copy(
                        isLoading = false,
                        currentLocation = result.data,
                        error = null
                    )
                }

                is Result.Error -> {
                    val message = errorMapper.run { result.error.toUserMessage() }
                    Log.w(TAG, "현재 위치 불러오기 실패: 메시지=$message, 원본오류=${result.error}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = message
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
        Log.d(TAG, "위치 권한이 허용되었습니다.")
        _state.value = _state.value.copy(isLocationPermissionGranted = true)
        loadCurrentLocation()
    }

    /**
     * 정류장 마커를 클릭했을 때 호출됩니다.
     * 선택된 정류장 정보를 표시하고 해당 정류장의 버스 정보를 로드합니다.
     */
    fun onBusStopSelected(busStop: com.tukorea.bus.domain.model.BusStop) {
        Log.d(TAG, "정류장 선택: ${busStop.name}")
        _state.value = _state.value.copy(
            selectedBusStop = busStop,
            isBusStopModalVisible = true,
            busesForSelectedStop = emptyList() // 초기화
        )
        loadBusesForStop(busStop.id)
    }
    
    /**
     * 특정 정류장의 버스 정보를 로드합니다.
     */
    private fun loadBusesForStop(busStopId: String) {
        Log.d(TAG, "정류장 버스 정보 불러오기 시작: $busStopId")
        viewModelScope.launch {
            try {
                val buses = getBusesForStopUseCase(busStopId)
                Log.d(TAG, "정류장 버스 정보 불러오기 성공: ${buses.size}개")
                _state.value = _state.value.copy(busesForSelectedStop = buses)
            } catch (e: Exception) {
                Log.e(TAG, "정류장 버스 정보 불러오기 실패", e)
                // 버스 정보 로드 실패는 치명적이지 않으므로 에러를 표시하지 않음
            }
        }
    }

    /**
     * 정류장 정보 모달을 닫습니다.
     */
    fun closeBusStopModal() {
        Log.d(TAG, "정류장 모달 닫기")
        _state.value = _state.value.copy(
            isBusStopModalVisible = false
        )
    }
}

