package com.tukorea.bus.ui.quickride

/**
 * QuickRide 화면에서 사용하는 UI 전용 모델/타입 모음
 * (도메인 모델의 String 기반 필드를 UI 친화적인 타입으로 변환)
 */

enum class BusRouteUi(val label: String) {
    A("A노선"),
    B("B노선"),
    C("C노선"),
    LOOP("순환"),
    UNKNOWN("");

    companion object {
        fun fromRaw(route: String): BusRouteUi = when (route) {
            A.label -> A
            B.label -> B
            C.label -> C
            LOOP.label -> LOOP
            else -> UNKNOWN
        }
    }
}

enum class SeatStatusUi(val label: String) {
    PLENTY("여유"),
    NORMAL("보통"),
    CROWDED("혼잡"),
    UNKNOWN("");

    companion object {
        fun fromRaw(seats: String): SeatStatusUi = when (seats) {
            PLENTY.label -> PLENTY
            NORMAL.label -> NORMAL
            CROWDED.label -> CROWDED
            else -> UNKNOWN
        }
    }
}


