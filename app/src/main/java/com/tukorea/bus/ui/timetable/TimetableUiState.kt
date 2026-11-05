package com.tukorea.bus.ui.timetable

import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ShuttleDirection

data class TimeCell(
    val departure: Departure,
    val isPast: Boolean,
    val isNext: Boolean,
)

data class HourRow(val hour: Int, val cells: List<TimeCell>)

data class TimetableUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val title: String = "",
    val period: String? = null,
    val selectedDayType: DayType = DayType.WEEKDAY,
    val directions: List<ShuttleDirection> = emptyList(),
    val selectedDirection: ShuttleDirection? = null,
    val onDemand: List<Departure> = emptyList(),
    val hours: List<HourRow> = emptyList(),
    /** 시간마다 1~2대뿐인 노선은 시간대로 묶지 않고 한 덩어리로 보여 준다(빈 줄이 늘어나지 않게). */
    val isCompact: Boolean = false,
    val notes: List<Departure> = emptyList(),
    /** 고른 요일이 오늘이면 지난 시간과 다음 시간을 표시한다. */
    val isToday: Boolean = false,
)
