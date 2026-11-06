package com.tukorea.bus.ui.timetable

import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ShuttleDirection

/**
 * 시간표 한 줄. 정시 출발과 수시운행 구간을 같은 목록에 시각 순으로 둔다.
 * [minutesUntil]은 오늘 시간표일 때만 있다(지난 줄, 다른 요일이면 null).
 */
data class DepartureRow(
    val departure: Departure,
    val isPast: Boolean,
    val isNext: Boolean,
    val isRunningNow: Boolean,
    val minutesUntil: Int?,
) {
    val key: String get() = "${departure.minute}-${departure.endMinute}"
}

data class TimetableUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val title: String = "",
    val period: String? = null,
    val selectedDayType: DayType = DayType.WEEKDAY,
    val directions: List<ShuttleDirection> = emptyList(),
    val selectedDirection: ShuttleDirection? = null,
    /** 지난 출발. 오늘 시간표일 때만 채워지고 기본은 접혀 있다. */
    val past: List<DepartureRow> = emptyList(),
    val showPast: Boolean = false,
    val upcoming: List<DepartureRow> = emptyList(),
)
