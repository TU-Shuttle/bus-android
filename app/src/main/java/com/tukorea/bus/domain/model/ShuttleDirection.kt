package com.tukorea.bus.domain.model

/** 서버 시간표의 방향 코드와 이름이 같다. */
enum class ShuttleDirection(val origin: ShuttleStop, val destination: ShuttleStop) {
    STATION_TO_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS1),
    CAMPUS1_TO_STATION(ShuttleStop.CAMPUS1, ShuttleStop.JEONGWANG_STATION),
    CAMPUS1_TO_CAMPUS2(ShuttleStop.CAMPUS1, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.CAMPUS1),
    STATION_TO_CAMPUS2_VIA_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_STATION_VIA_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.JEONGWANG_STATION),
    CAMPUS2_TO_OIDO_STATION(ShuttleStop.CAMPUS2, ShuttleStop.OIDO_STATION),
    ;

    /** 지나는 순서대로의 정류장. 경유 노선은 본교 서문 정류장을 거친다. */
    val stops: List<ShuttleStop>
        get() = when (this) {
            STATION_TO_CAMPUS2_VIA_CAMPUS1, CAMPUS2_TO_STATION_VIA_CAMPUS1 ->
                listOf(origin, ShuttleStop.CAMPUS1_WEST_GATE, destination)
            else -> listOf(origin, destination)
        }
}
