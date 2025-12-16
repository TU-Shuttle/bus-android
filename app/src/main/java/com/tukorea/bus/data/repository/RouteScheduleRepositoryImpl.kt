package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.RouteScheduleTemplate
import com.tukorea.bus.domain.repository.RouteScheduleRepository
import javax.inject.Inject

/**
 * 노선별 시간표를 하드코딩된 리스트로 제공하는 임시 구현체
 */
class RouteScheduleRepositoryImpl @Inject constructor() : RouteScheduleRepository {

    override suspend fun getRouteSchedules(): List<RouteScheduleTemplate> {
        return listOf(
            RouteScheduleTemplate(
                name = "A노선",
                morningTimes = listOf("09:00", "09:30", "10:00", "10:30", "11:00", "11:30"),
                afternoonTimes = listOf(
                    "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
                )
            ),
            RouteScheduleTemplate(
                name = "B노선",
                morningTimes = listOf("09:15", "09:45", "10:15", "10:45", "11:15", "11:45"),
                afternoonTimes = listOf(
                    "13:15", "13:45", "14:15", "14:45", "15:15", "15:45", "16:15", "16:45", "17:15"
                )
            ),
            RouteScheduleTemplate(
                name = "C노선",
                morningTimes = listOf("09:20", "10:00", "10:40", "11:20"),
                afternoonTimes = listOf(
                    "13:20", "14:00", "14:40", "15:20", "16:00", "16:40", "17:20"
                )
            ),
            RouteScheduleTemplate(
                name = "순환",
                morningTimes = listOf(
                    "09:00", "09:20", "09:40", "10:00", "10:20", "10:40", "11:00", "11:20", "11:40"
                ),
                afternoonTimes = listOf(
                    "13:00",
                    "13:20",
                    "13:40",
                    "14:00",
                    "14:20",
                    "14:40",
                    "15:00",
                    "15:20",
                    "15:40",
                    "16:00",
                    "16:20",
                    "16:40",
                    "17:00",
                    "17:20",
                    "17:40"
                )
            )
        )
    }
}


