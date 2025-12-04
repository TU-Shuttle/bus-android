package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캘린더(예약 입력 폼) Repository 구현체
 */
@Singleton
class CalendarRepositoryImpl @Inject constructor() : CalendarRepository {

    override fun getDays(): Flow<List<String>> {
        val operatingDays = listOf("월", "화", "수", "목", "금", "토", "일")
        return flowOf(operatingDays)
    }

    override fun getTimes(): Flow<List<String>> {
        val operatingTimes = listOf(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30", "18:00", "18:30"
        )
        return flowOf(operatingTimes)
    }

    override fun getLocations(): Flow<List<String>> {
        val stations = listOf("1캠퍼스", "2캠퍼스", "정왕역", "오이도역")
        return flowOf(stations)
    }
}

