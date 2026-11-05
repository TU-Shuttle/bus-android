package com.tukorea.bus.domain.model

/**
 * 현재 적용 중인 시간표.
 * @param validFrom 적용 시작일(yyyy-MM-dd). 모르면 null
 * @param validTo 적용 종료일(yyyy-MM-dd). 모르면 null
 * @param fetchedAt 서버가 학교 홈페이지에서 수집한 시각(yyyy-MM-ddTHH:mm...). 모르면 null
 */
data class Timetable(
    val title: String,
    val validFrom: String?,
    val validTo: String?,
    val fetchedAt: String?,
    val departures: List<Departure>,
) {
    /** 날짜가 적용 기간 밖이면 false. 기간을 모르면 true. ISO 날짜라 문자열 비교로 충분하다. */
    fun isValidOn(isoDate: String): Boolean {
        if (validFrom != null && isoDate < validFrom) return false
        if (validTo != null && isoDate > validTo) return false
        return true
    }

    fun directionsOn(dayType: DayType): List<ShuttleDirection> =
        ShuttleDirection.entries.filter { direction ->
            departures.any { it.dayType == dayType && it.direction == direction }
        }
}
