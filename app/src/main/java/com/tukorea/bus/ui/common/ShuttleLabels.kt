package com.tukorea.bus.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop

@StringRes
fun ShuttleStop.nameRes(): Int = when (this) {
    ShuttleStop.JEONGWANG_STATION -> R.string.stop_jeongwang_station
    ShuttleStop.CAMPUS1 -> R.string.stop_campus1
    ShuttleStop.CAMPUS1_WEST_GATE -> R.string.stop_campus1_west_gate
    ShuttleStop.CAMPUS2 -> R.string.stop_campus2
    ShuttleStop.OIDO_STATION -> R.string.stop_oido_station
}

@StringRes
fun ShuttleStop.boardingRes(): Int = when (this) {
    ShuttleStop.JEONGWANG_STATION -> R.string.stop_boarding_jeongwang_station
    ShuttleStop.CAMPUS1 -> R.string.stop_boarding_campus1
    ShuttleStop.CAMPUS1_WEST_GATE -> R.string.stop_boarding_campus1_west_gate
    ShuttleStop.CAMPUS2 -> R.string.stop_boarding_campus2
    ShuttleStop.OIDO_STATION -> R.string.stop_oido_station
}

@StringRes
fun DayType.labelRes(): Int = when (this) {
    DayType.WEEKDAY -> R.string.day_type_weekday
    DayType.SATURDAY -> R.string.day_type_saturday
}

private val ShuttleDirection.isVia: Boolean
    get() = this == ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1 ||
        this == ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1

/** "정왕역 → 본교", 경유 노선은 "정왕역 → 본교 → 제2캠퍼스". */
@Composable
fun ShuttleDirection.label(): String {
    val from = stringResource(origin.nameRes())
    val to = stringResource(destination.nameRes())
    return stringResource(if (isVia) R.string.direction_via_label else R.string.direction_label, from, to)
}

/** {1, 3} → "월·수" */
@Composable
fun weekdaysLabel(isoDays: Set<Int>): String {
    val names = stringResource(R.string.weekday_short_names).split(",")
    return isoDays.sorted().joinToString("·") { names[it - 1] }
}

/** 자정부터의 분 → "09:40" */
fun formatMinute(minuteOfDay: Int): String = "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

/** "2026-09-01" → "9.1" */
fun formatShortDate(isoDate: String?): String? {
    val parts = isoDate?.take(10)?.split("-") ?: return null
    if (parts.size != 3) return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    return "$month.$day"
}
