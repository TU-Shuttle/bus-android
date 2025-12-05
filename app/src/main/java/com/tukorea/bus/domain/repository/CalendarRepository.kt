package com.tukorea.bus.domain.repository

import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getDays(): Flow<List<String>>
    fun getTimes(): Flow<List<String>>
    fun getTimesByType(scheduleType: String): Flow<List<String>>
    fun getLocations(): Flow<List<String>>
}

