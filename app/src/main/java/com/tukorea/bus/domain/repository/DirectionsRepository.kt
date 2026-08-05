package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Coordinate
import com.tukorea.bus.domain.model.Directions
import com.tukorea.bus.domain.util.AppResult

/**
 * 길찾기 Repository 인터페이스
 */
interface DirectionsRepository {
    /**
     * 출발지에서 도착지까지의 경로를 조회합니다.
     *
     * @param startLat 출발지 위도
     * @param startLng 출발지 경도
     * @param endLat 도착지 위도
     * @param endLng 도착지 경도
     * @param waypoints 경유지 좌표 목록 (최대 5개)
     * @return 길찾기 결과
     */
    suspend fun getDirections(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        waypoints: List<Coordinate> = emptyList()
    ): AppResult<Directions>
}
