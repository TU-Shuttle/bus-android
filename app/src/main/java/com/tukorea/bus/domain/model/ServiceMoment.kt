package com.tukorea.bus.domain.model

/**
 * 셔틀 기준(한국 시간) 현재 시점.
 * @param isoDate yyyy-MM-dd
 * @param isoDayOfWeek 1=월요일 … 7=일요일
 * @param minuteOfDay 자정부터의 분
 */
data class ServiceMoment(
    val isoDate: String,
    val isoDayOfWeek: Int,
    val minuteOfDay: Int,
) {
    val dayType: DayType? get() = DayType.of(isoDayOfWeek)
}
