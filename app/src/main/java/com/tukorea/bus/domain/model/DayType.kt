package com.tukorea.bus.domain.model

enum class DayType {
    WEEKDAY,
    SATURDAY,
    ;

    companion object {
        /** @param isoDayOfWeek 1=월요일 … 7=일요일. 일요일은 운행하지 않아 null. */
        fun of(isoDayOfWeek: Int): DayType? =
            when (isoDayOfWeek) {
                in 1..5 -> WEEKDAY
                6 -> SATURDAY
                else -> null
            }
    }
}
