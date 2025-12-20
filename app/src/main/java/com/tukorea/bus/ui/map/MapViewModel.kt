package com.tukorea.bus.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.model.MapLocation
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
        loadBusRoutes()
        loadBusStops()
    }
    
    /**
     * 버스 경로를 로드합니다.
     * 임시로 예시 경로 데이터를 사용합니다.
     */
    fun loadBusRoutes() {
        Log.d(TAG, "버스 경로 로드 시작")
        // 임시 예시 데이터 (실제로는 Repository에서 가져와야 함)
        val routes = listOf(
            BusRoute(
                routeName = "하교 방향",
                coordinates = listOf(
                    MapLocation(37.2938, 126.8395, 0f), // 2캠퍼스 셔틀버스 정류장 근처
                    MapLocation(37.2980, 126.8450, 0f), // 1캠퍼스 (산융) 근처
                    MapLocation(37.3000, 126.8500, 0f), // 정왕역 근처
                )
            ),
            BusRoute(
                routeName = "등교 방향",
                coordinates = listOf(
                    MapLocation(37.3000, 126.8500, 0f), // 정왕역 근처
                    MapLocation(37.2980, 126.8450, 0f), // 1캠퍼스 (산융) 근처
                    MapLocation(37.2938, 126.8395, 0f), // 2캠퍼스 셔틀버스 정류장 근처
                )
            )
        )
        _state.value = _state.value.copy(busRoutes = routes)
        Log.d(TAG, "버스 경로 로드 완료: ${routes.size}개 노선")
    }

    /**
     * 정류장 목록을 로드합니다.
     * 임시로 예시 데이터를 사용합니다.
     */
    fun loadBusStops() {
        Log.d(TAG, "정류장 목록 로드 시작")
        // 임시 예시 데이터 (실제로는 Repository에서 가져와야 함)
        val busStops = listOf(
            com.tukorea.bus.domain.model.BusStop(
                id = "jeongwang_station",
                name = "정왕역 탑승장소",
                latitude = 37.33949,
                longitude = 126.7326,
                description = "1캠퍼스 → 정왕역",
                destinations = listOf("정왕역")
            ),
            com.tukorea.bus.domain.model.BusStop(
                id = "campus2_direction_after_10",
                name = "제2캠퍼스 방향 탑승장소",
                latitude = 37.33862,
                longitude = 126.7340,
                description = "1캠퍼스 → 2캠퍼스 (오전 10시 이후)",
                destinations = listOf("2캠퍼스"),
                operatingTimeStart = "10:00"
            ),
            com.tukorea.bus.domain.model.BusStop(
                id = "campus2_main",
                name = "제2캠퍼스",
                latitude = 37.32802,
                longitude = 126.6884,
                description = "2캠퍼스 → 1캠퍼스 → 정왕역",
                destinations = listOf("1캠퍼스", "정왕역"),
                operatingTimeEnd = "09:30"
            ),
            com.tukorea.bus.domain.model.BusStop(
                id = "campus1_to_campus2",
                name = "1캠퍼스",
                latitude = 37.34148,
                longitude = 126.7308,
                description = "1캠퍼스 → 2캠퍼스 (오전 9시 30분까지)",
                destinations = listOf("2캠퍼스"),
                operatingTimeEnd = "09:30"
            ),
            com.tukorea.bus.domain.model.BusStop(
                id = "jeongwang_to_main_before_5pm",
                name = "정왕역 → 본교 (오후 5시 전)",
                latitude = 37.35187,
                longitude = 126.7415,
                description = "정왕역 → 본교",
                destinations = listOf("본교"),
                operatingTimeEnd = "17:00"
            ),
            com.tukorea.bus.domain.model.BusStop(
                id = "jeongwang_to_main_after_5pm",
                name = "정왕역 → 본교 (오후 5시 후)",
                latitude = 37.35112,
                longitude = 126.7415,
                description = "정왕역 → 본교",
                destinations = listOf("본교"),
                operatingTimeStart = "17:00"
            ),
            com.tukorea.bus.domain.model.BusStop(
                id = "jeongwang_to_main_to_campus2",
                name = "정왕역 → 본교 → 2캠",
                latitude = 37.35079,
                longitude = 126.7430,
                description = "정왕역 → 본교 → 2캠퍼스",
                destinations = listOf("본교", "2캠퍼스")
            )
        )
        _state.value = _state.value.copy(busStops = busStops)
        Log.d(TAG, "정류장 목록 로드 완료: ${busStops.size}개 정류장")
    }

    /**
     * 정류장 마커를 클릭했을 때 호출됩니다.
     * 선택된 정류장 정보를 표시합니다.
     */
    fun onBusStopSelected(busStop: com.tukorea.bus.domain.model.BusStop) {
        Log.d(TAG, "정류장 선택: ${busStop.name}")
        _state.value = _state.value.copy(
            selectedBusStop = busStop,
            isBusStopModalVisible = true
        )
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

