package com.tukorea.bus.ui.timetable

import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ShuttleDirection

data class TimeCell(
    val departure: Departure,
    val isPast: Boolean,
    val isNext: Boolean,
)

/** 시간표 목록의 한 줄. 정시 출발은 시간대별로 묶고, 수시운행 구간은 그 시각 자리에 한 줄로 끼운다. */
sealed interface TimetableEntry {
    val key: String

    data class Hour(val hour: Int, val cells: List<TimeCell>) : TimetableEntry {
        override val key = "h-$hour"
        val isPast: Boolean get() = cells.all { it.isPast }
    }

    data class OnDemand(val departure: Departure, val isPast: Boolean) : TimetableEntry {
        override val key = "od-${departure.minute}"
    }
}

data class TimetableUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val title: String = "",
    val period: String? = null,
    val selectedDayType: DayType = DayType.WEEKDAY,
    val directions: List<ShuttleDirection> = emptyList(),
    val selectedDirection: ShuttleDirection? = null,
    val entries: List<TimetableEntry> = emptyList(),
    /** 처음 보여 줄 줄(다음 출발이 있는 시간대). 오늘이 아니거나 운행이 끝났으면 null. */
    val focusIndex: Int? = null,
    val notes: List<Departure> = emptyList(),
)
