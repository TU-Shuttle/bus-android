package com.tukorea.bus.domain.model

/**
 * 정류장의 버스 정보를 나타내는 데이터 클래스
 */
data class BusStopBusInfo(
    val route: String,
    val status: BusStatus,
    val time: String,
    val destination: String
)

/**
 * 버스 상태를 나타내는 enum
 */
enum class BusStatus {
    RUNNING,    // 운행
    WAITING,    // 대기
    FINISHED    // 운행 종료
}
