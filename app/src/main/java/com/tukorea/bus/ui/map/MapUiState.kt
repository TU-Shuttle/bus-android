package com.tukorea.bus.ui.map

import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.MapLocation
import com.tukorea.bus.domain.repository.BusStopBusInfo

/**
 * 맵 화면의 UI 상태를 관리하는 데이터 클래스
 * @param isLoading 위치 로딩 중 여부
 * @param currentLocation 현재 위치 정보
 * @param isLocationPermissionGranted 위치 권한 허용 여부
 * @param error 에러 메시지 (에러 발생 시)
 * @param selectedBusStop 선택된 정류장 (마커 클릭 시)
 * @param isBusStopModalVisible 정류장 정보 모달 표시 여부
 * @param busStops 정류장 목록
 * @param busesForSelectedStop 선택된 정류장의 버스 목록
 */
data class MapUiState(
    val isLoading: Boolean = false,
    val currentLocation: MapLocation? = null,
    val isLocationPermissionGranted: Boolean = false,
    val error: String? = null,
    val selectedBusStop: BusStop? = null,
    val isBusStopModalVisible: Boolean = false,
    val busStops: List<BusStop> = emptyList(),
    val busesForSelectedStop: List<BusStopBusInfo> = emptyList()
)

