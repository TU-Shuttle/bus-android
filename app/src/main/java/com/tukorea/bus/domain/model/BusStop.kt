package com.tukorea.bus.domain.model

/**
 * 버스 정류장 정보를 나타내는 도메인 모델
 *
 * @property id 정류장 고유 식별자
 * @property name 정류장 이름
 * @property latitude 위도
 * @property longitude 경도
 * @property description 정류장 설명 (노선 정보 등)
 * @property destinations 이 정류장에서 갈 수 있는 목적지 목록
 * @property operatingTimeStart 운행 시작 시간 (선택적, 예: "09:30")
 * @property operatingTimeEnd 운행 종료 시간 (선택적, 예: "10:00")
 */
data class BusStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val destinations: List<String>,
    val operatingTimeStart: String? = null,
    val operatingTimeEnd: String? = null
)
