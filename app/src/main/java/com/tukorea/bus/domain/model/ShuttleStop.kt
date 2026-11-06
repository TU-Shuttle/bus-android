package com.tukorea.bus.domain.model

/**
 * 셔틀 탑승 장소. 좌표는 지도 표시용 근사값이다.
 */
enum class ShuttleStop(val latitude: Double, val longitude: Double) {
    /** 정왕역 꽃집 앞. 17시 이후 등교 셔틀은 파리바게뜨 건너편 하차장소에서 탄다. */
    JEONGWANG_STATION(37.35187, 126.7415),

    /** 본교 산융관 앞 정류장. 정왕역·제2캠퍼스 방향 승차장이 같다. */
    CAMPUS1(37.33949, 126.7326),

    /** 본교 운동장 옆(서문) 버스정류장. 정왕역↔제2캠퍼스 경유 노선이 승·하차한다. */
    CAMPUS1_WEST_GATE(37.33862, 126.7340),

    /** 제2캠퍼스(시흥 배곧) 정류장. */
    CAMPUS2(37.32802, 126.6884),

    /** 오이도역. 제2캠퍼스 17:40 출발 셔틀의 종점(본교를 거치지 않음). */
    OIDO_STATION(37.36236, 126.73871),
    ;

    val point: GeoPoint get() = GeoPoint(latitude, longitude)
}
