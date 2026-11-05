package com.tukorea.bus.domain.model

/** 서버 시간표의 방향 코드와 이름이 같다. */
enum class ShuttleDirection(val origin: ShuttleStop, val destination: ShuttleStop) {
    STATION_TO_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS1),
    CAMPUS1_TO_STATION(ShuttleStop.CAMPUS1, ShuttleStop.JEONGWANG_STATION),
    CAMPUS1_TO_CAMPUS2(ShuttleStop.CAMPUS1, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.CAMPUS1),
    STATION_TO_CAMPUS2_VIA_CAMPUS1(ShuttleStop.JEONGWANG_STATION, ShuttleStop.CAMPUS2),
    CAMPUS2_TO_STATION_VIA_CAMPUS1(ShuttleStop.CAMPUS2, ShuttleStop.JEONGWANG_STATION),
}
