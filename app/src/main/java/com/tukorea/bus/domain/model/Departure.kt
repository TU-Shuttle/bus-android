package com.tukorea.bus.domain.model

/**
 * 시간표 한 줄. 시각은 자정부터의 분(0..1439).
 * [endMinute]이 있으면 정시 출발이 아니라 [minute]~[endMinute] 동안 수시로 운행하는 구간이다.
 */
data class Departure(
    val direction: ShuttleDirection,
    val dayType: DayType,
    val minute: Int,
    val endMinute: Int? = null,
    val note: String? = null,
) {
    val isOnDemand: Boolean get() = endMinute != null
}
