package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Coordinate
import com.tukorea.bus.domain.model.Directions
import com.tukorea.bus.domain.repository.DirectionsRepository
import com.tukorea.bus.domain.util.AppResult
import javax.inject.Inject

/**
 * 길찾기 UseCase
 */
class GetDirectionsUseCase @Inject constructor(
    private val repository: DirectionsRepository
) {
    /**
     * 출발지에서 도착지까지의 경로를 조회합니다.
     *
     * @param start 출발지 좌표
     * @param end 도착지 좌표
     * @param waypoints 경유지 좌표 목록 (최대 5개)
     * @return 길찾기 결과
     */
    suspend operator fun invoke(
        start: Coordinate,
        end: Coordinate,
        waypoints: List<Coordinate> = emptyList()
    ): AppResult<Directions> {
        return repository.getDirections(
            startLat = start.latitude,
            startLng = start.longitude,
            endLat = end.latitude,
            endLng = end.longitude,
            waypoints = waypoints
        )
    }

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
    suspend operator fun invoke(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        waypoints: List<Coordinate> = emptyList()
    ): AppResult<Directions> {
        return repository.getDirections(
            startLat = startLat,
            startLng = startLng,
            endLat = endLat,
            endLng = endLng,
            waypoints = waypoints
        )
    }
}
