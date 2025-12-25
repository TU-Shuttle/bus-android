package com.tukorea.bus.domain.model

/**
 * 길찾기 결과 Domain 모델
 */
data class Directions(
    val message: String,
    val routes: List<Route>
)

/**
 * 경로 정보
 */
data class Route(
    val type: RouteType,
    val summary: RouteSummaryInfo,
    val path: List<Coordinate>,
    val sections: List<SectionInfo>,
    val guides: List<GuideInfo>
)

/**
 * 경로 타입
 */
enum class RouteType {
    TRAFAST,      // 실시간 빠른길
    TRAOPTIMAL,   // 실시간 최적
    TRACOMFORT    // 실시간 편한길
}

/**
 * 경로 요약 정보
 */
data class RouteSummaryInfo(
    val startLocation: Coordinate,
    val goalLocation: Coordinate,
    val totalDistanceMeters: Int,      // 총 거리 (미터)
    val totalDurationMinutes: Int,     // 총 소요 시간 (분)
    val departureTime: String,
    val tollFare: Int,                 // 톨게이트 요금
    val taxiFare: Int,                 // 예상 택시 요금
    val fuelPrice: Int                 // 예상 연료비
)

/**
 * 좌표 (위도, 경도)
 */
data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

/**
 * 구간 정보
 */
data class SectionInfo(
    val name: String,                  // 도로명
    val distance: Int,                 // 구간 거리 (미터)
    val congestion: CongestionLevel,   // 혼잡도
    val speed: Int                     // 속도 (km/h)
)

/**
 * 혼잡도 레벨
 */
enum class CongestionLevel {
    SMOOTH,    // 원활
    SLOW,      // 서행
    DELAY,     // 지체
    JAM,       // 정체
    UNKNOWN
}

/**
 * 안내 정보
 */
data class GuideInfo(
    val instructions: String,          // 안내 문구
    val distance: Int,                 // 거리 (미터)
    val durationMinutes: Int           // 소요 시간 (분)
)
