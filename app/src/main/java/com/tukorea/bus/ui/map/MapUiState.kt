package com.tukorea.bus.ui.map

import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.MapLocation

/**
 * 버스 경로 정보 (정류장 위치 목록)
 */
data class BusRoute(
    val routeName: String,
    val coordinates: List<MapLocation> // 경로상의 좌표 목록
)

/**
 * 맵 화면의 UI 상태를 관리하는 데이터 클래스
 * @param isLoading 위치 로딩 중 여부
 * @param currentLocation 현재 위치 정보
 * @param isLocationPermissionGranted 위치 권한 허용 여부
 * @param error 에러 메시지 (에러 발생 시)
 * @param busRoutes 표시할 버스 경로 목록
 * @param busStops 표시할 정류장 목록
 * @param selectedBusStop 선택된 정류장 (마커 클릭 시)
 * @param isBusStopModalVisible 정류장 정보 모달 표시 여부
 */
data class MapUiState(
    val isLoading: Boolean = false,
    val currentLocation: MapLocation? = null,
    val isLocationPermissionGranted: Boolean = false,
    val error: String? = null,
    val busRoutes: List<BusRoute> = emptyList(),
    val busStops: List<BusStop> = emptyList(),
    val selectedBusStop: BusStop? = null,
    val isBusStopModalVisible: Boolean = false
)

