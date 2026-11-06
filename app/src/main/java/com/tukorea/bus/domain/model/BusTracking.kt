package com.tukorea.bus.domain.model

/**
 * 버스 한 대를 골랐을 때 보여 줄 운행 정보.
 * 경로 위에 있으면 [nextStop]과 남은 거리·시간을, 경로를 벗어나 있으면 null로 둔다.
 */
data class BusTracking(
    val bus: LiveBus,
    /** 진행 방향(°). 직전 위치가 없거나 거의 움직이지 않았으면 null. */
    val headingDegrees: Double?,
    /** 직전 두 위치로 잰 속도. */
    val speedKmh: Int?,
    /** 바로 다음에 닿을 정류장(경유 노선이면 중간 정류장일 수 있다). */
    val nextStop: ShuttleStop?,
    /** [nextStop]까지 남은 거리·시간. */
    val remainingMeters: Int?,
    val etaMinutes: Int?,
    /** 다음 정류장이 종점이 아닐 때만 종점과 종점까지의 시간. */
    val finalStop: ShuttleStop? = null,
    val finalEtaMinutes: Int? = null,
)
