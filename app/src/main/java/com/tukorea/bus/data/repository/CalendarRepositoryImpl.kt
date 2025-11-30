package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
) : CalendarRepository {
    
    override fun getDays(): Flow<List<String>> {
        val dummyDays = listOf("월", "화", "수", "목", "금", "토", "일")
        return flowOf(dummyDays)
    }
    
    override fun getTimes(): Flow<List<String>> {
        val dummyTimes = listOf(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
            "16:00", "16:30", "17:00", "17:30", "18:00", "18:30"
        )
        return flowOf(dummyTimes)
    }
    
    override fun getLocations(): Flow<List<String>> {
        val dummyLocations = listOf("기숙사", "본관", "역", "도서관", "학생회관", "정문")
        return flowOf(dummyLocations)
    }
}

