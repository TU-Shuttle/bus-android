package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.model.UpcomingDepartures
import javax.inject.Inject

/** 지금 시각 기준으로 한 방향의 다음 출발들을 계산한다. */
class GetUpcomingDeparturesUseCase @Inject constructor() {

    operator fun invoke(
        timetable: Timetable,
        direction: ShuttleDirection,
        now: ServiceMoment,
        limit: Int = DEFAULT_LIMIT,
    ): UpcomingDepartures {
        val dayType = now.dayType ?: return UpcomingDepartures.NoService
        val today = timetable.departures
            .filter { it.dayType == dayType && it.direction == direction }
            .sortedBy { it.minute }
        if (today.isEmpty()) return UpcomingDepartures.NoService

        val onDemandUntil = today
            .firstOrNull { it.isOnDemand && now.minuteOfDay in it.minute until it.endMinute!! }
            ?.endMinute
        val next = today
            .filter { !it.isOnDemand && it.minute >= now.minuteOfDay }
            .take(limit)
        val upcomingWindow = today.any { it.isOnDemand && it.minute > now.minuteOfDay }

        if (onDemandUntil == null && next.isEmpty() && !upcomingWindow) {
            return UpcomingDepartures.Finished(firstDeparture = today.first())
        }
        return UpcomingDepartures.Scheduled(onDemandUntil = onDemandUntil, next = next)
    }

    private companion object {
        const val DEFAULT_LIMIT = 3
    }
}
