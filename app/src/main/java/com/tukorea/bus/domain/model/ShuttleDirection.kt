package com.tukorea.bus.domain.model

/** 서버 시간표의 방향 코드와 이름이 같다. */
enum class ShuttleDirection(val origin: ShuttleStop, val destination: ShuttleStop) {
    STATION_TO_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS1),
    CAMPUS1_TO_STATION(ShuttleStop.CAMPUS1, ShuttleStop.JEONGWANG_STATION),
    CAMPUS1_TO_CAMPUS2(ShuttleStop.CAMPUS1, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.CAMPUS1),
    STATION_TO_CAMPUS2_VIA_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_STATION_VIA_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.JEONGWANG_STATION),
    ;

    /** 내릴 수 있는 곳. 경유 노선은 본교 서문에서도 내릴 수 있다(서버 규칙과 같다). */
    val dropOffOptions: List<ShuttleStop>
        get() = when (this) {
            STATION_TO_CAMPUS2_VIA_CAMPUS1 -> listOf(ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.CAMPUS2)
            CAMPUS2_TO_STATION_VIA_CAMPUS1 -> listOf(ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.JEONGWANG_STATION)
            else -> listOf(destination)
        }
}
