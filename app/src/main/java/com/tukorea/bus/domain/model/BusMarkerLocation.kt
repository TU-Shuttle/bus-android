package com.tukorea.bus.domain.model

/**
 * 버스 위치 정보를 담는 데이터 클래스
 * Domain 레이어에서 사용하는 모델입니다.
 */
data class BusMarkerLocation(
    val latitude: Double,
    val longitude: Double,
    val caption: String? = null
)

