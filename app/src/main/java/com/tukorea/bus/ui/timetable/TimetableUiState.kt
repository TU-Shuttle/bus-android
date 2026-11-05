package com.tukorea.bus.ui.timetable

import androidx.annotation.StringRes
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.ui.ride.RideSheetState

data class TimeCell(
    val departure: Departure,
    val isPast: Boolean,
    val isNext: Boolean,
    val isReserved: Boolean,
) {
    /** 오늘 남은 정시 출발만 탑승 예정을 남길 수 있다. */
    val isReservable: Boolean get() = !isPast
}

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
    val notes: List<Departure> = emptyList(),
    /** 고른 요일이 오늘이면 지난 시간/다음 시간을 표시하고 탑승 예정을 받을 수 있다. */
    val isToday: Boolean = false,
    val rideSheet: RideSheetState? = null,
    @StringRes val messageRes: Int? = null,
)
