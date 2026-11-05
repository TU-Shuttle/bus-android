package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.Timetable
import javax.inject.Inject

/**
 * 처음 화면을 열었을 때 고를 방향. 오전엔 등교, 오후엔 하교 방향을 권하고,
 * 그날 운행하는 방향 중에서만 고른다.
 */
class SuggestDirectionUseCase @Inject constructor() {

    operator fun invoke(timetable: Timetable, now: ServiceMoment): ShuttleDirection? {
        val dayType = now.dayType ?: return null
        val available = timetable.directionsOn(dayType)
        if (available.isEmpty()) return null

        val morning = now.minuteOfDay < NOON
        val preferred = if (morning) MORNING_PREFERENCE else AFTERNOON_PREFERENCE
        return preferred.firstOrNull { it in available } ?: available.first()
    }

    private companion object {
        const val NOON = 12 * 60
        val MORNING_PREFERENCE = listOf(
            ShuttleDirection.STATION_TO_CAMPUS1,
            ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
        )
        val AFTERNOON_PREFERENCE = listOf(
            ShuttleDirection.CAMPUS1_TO_STATION,
            ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1,
        )
    }
}
