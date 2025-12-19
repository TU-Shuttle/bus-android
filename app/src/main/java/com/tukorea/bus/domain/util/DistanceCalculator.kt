package com.tukorea.bus.domain.util

import kotlin.math.*

/**
 * 지구 상의 두 지점 간 거리를 계산하는 유틸리티 객체
 */
object DistanceCalculator {
    /**
     * 지구 반지름 (킬로미터)
     */
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * 하버사인 공식(Haversine Formula)을 사용하여 두 지점 간의 거리를 계산합니다.
     * 지구를 완전한 구로 가정하여 대권 거리(Great Circle Distance)를 계산합니다.
     *
     * @param lat1 첫 번째 지점의 위도 (도 단위)
     * @param lon1 첫 번째 지점의 경도 (도 단위)
     * @param lat2 두 번째 지점의 위도 (도 단위)
     * @param lon2 두 번째 지점의 경도 (도 단위)
     * @return 두 지점 간의 거리 (미터 단위)
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        // 위도와 경도를 라디안으로 변환
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // 위도 차이와 경도 차이
        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        // 하버사인 공식
        // a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)

        // c = 2 × atan2(√a, √(1−a))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // 거리 = 반지름 × c (킬로미터)
        val distanceKm = EARTH_RADIUS_KM * c

        // 미터 단위로 변환하여 반환
        return distanceKm * 1000
    }

    /**
     * 거리를 사용자 친화적인 문자열로 변환합니다.
     *
     * @param distanceMeters 거리 (미터 단위)
     * @return 포맷된 거리 문자열 (예: "150m", "1.2km")
     */
    fun formatDistance(distanceMeters: Double): String {
        return when {
            distanceMeters < 1000 -> "${distanceMeters.toInt()}m"
            else -> String.format("%.1fkm", distanceMeters / 1000)
        }
    }
}
