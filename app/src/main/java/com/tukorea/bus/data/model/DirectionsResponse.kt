package com.tukorea.bus.data.model

/**
 * 길찾기 API 응답 DTO
 * GET /api/bus/directions/{start}/{end}
 * 네이버 API 있는거 그대로 반환함
 */
data class DirectionsResponse(
    val data: DirectionsData?,
    val message: String?,
    val status: Int?,
    val timestamp: String?
)

data class DirectionsData(
    val code: Int?,
    val message: String?,
    val currentDateTime: String?,
    val route: RouteData?
)

data class RouteData(
    val trafast: List<RouteOption>?,
    val traoptimal: List<RouteOption>?,
    val tracomfort: List<RouteOption>?
)

data class RouteOption(
    val summary: RouteSummary?,
    val path: List<List<Double>>?,
    val section: List<RouteSection>?,
    val guide: List<RouteGuide>?
)

data class RouteSummary(
    val start: LocationPoint?,
    val goal: LocationPoint?,
    val distance: Int?,           // 총 거리 (미터)
    val duration: Long?,          // 총 소요 시간 (밀리초)
    val departureTime: String?,
    val bbox: List<List<Double>>?,
    val tollFare: Int?,           // 톨게이트 요금
    val taxiFare: Int?,           // 예상 택시 요금
    val fuelPrice: Int?           // 예상 연료비
)

data class LocationPoint(
    val location: List<Double>?,  // [경도, 위도]
    val dir: Int?                 // 방향
)

data class RouteSection(
    val pointIndex: Int?,
    val pointCount: Int?,
    val distance: Int?,
    val name: String?,
    val congestion: Int?,         // 혼잡도 (1: 원활, 2: 서행, 3: 지체, 4: 정체)
    val speed: Int?               // 속도 (km/h)
)

data class RouteGuide(
    val pointIndex: Int?,
    val type: Int?,               // 안내 타입
    val instructions: String?,    // 안내 문구
    val distance: Int?,
    val duration: Long?
)
