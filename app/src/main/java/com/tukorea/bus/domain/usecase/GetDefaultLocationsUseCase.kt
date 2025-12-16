package com.tukorea.bus.domain.usecase

import javax.inject.Inject

/**
 * 기본 출발지와 도착지를 선택하는 UseCase.
 */
class GetDefaultLocationsUseCase @Inject constructor() {
    /**
     * 기본 출발지와 도착지를 선택합니다.
     *
     * @param locations 사용 가능한 장소 목록
     * @param currentLocation 현재 선택된 출발지 (비어있으면 기본값 선택)
     * @param currentDestination 현재 선택된 도착지 (비어있으면 기본값 선택)
     * @return 기본 출발지와 도착지를 포함한 데이터 클래스
     */
    data class Result(
        val from: String,
        val to: String
    )

    /**
     * 장소 목록에서 기본 출발지와 도착지를 선택합니다.
     * - 출발지: 첫 번째 장소
     * - 도착지: 장소가 2개 이상이면 두 번째, 1개면 첫 번째
     *
     * @param locations 사용 가능한 장소 목록
     * @param currentLocation 현재 선택된 출발지
     * @param currentDestination 현재 선택된 도착지
     * @return 기본 출발지와 도착지
     */
    operator fun invoke(
        locations: List<String>,
        currentLocation: String = "",
        currentDestination: String = ""
    ): Result {
        val defaultFrom = locations.firstOrNull().orEmpty()
        val defaultTo = when {
            locations.size > 1 -> locations[1]
            locations.isNotEmpty() -> locations.first()
            else -> ""
        }

        return Result(
            from = if (currentLocation.isEmpty()) defaultFrom else currentLocation,
            to = when {
                currentDestination.isNotEmpty() -> currentDestination
                locations.size > 1 -> locations[1]
                locations.isNotEmpty() -> locations.first()
                else -> ""
            }
        )
    }
}

