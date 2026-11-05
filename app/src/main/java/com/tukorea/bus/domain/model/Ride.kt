package com.tukorea.bus.domain.model

/** 내가 남긴 '이 셔틀 탈게요'. 좌석 예약이 아니라 예정 인원 파악용이다. */
data class RideIntent(
    val id: Long,
    val serviceDate: String,
    val direction: ShuttleDirection,
    val departMinute: Int,
    val dropOff: ShuttleStop,
)

/** 한 출발편의 탑승 예정 인원. */
data class RideSummary(
    val direction: ShuttleDirection,
    val departMinute: Int,
    val riders: Int,
    val dropOffs: Map<ShuttleStop, Int>,
)

/** 탑승 예정을 남길 출발편. */
data class RideTarget(
    val serviceDate: String,
    val direction: ShuttleDirection,
    val departMinute: Int,
)
