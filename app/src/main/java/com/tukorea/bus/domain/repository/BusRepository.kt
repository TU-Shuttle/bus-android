package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Bus
import kotlinx.coroutines.flow.Flow

interface BusRepository {
    fun getAvailableBuses(from: String, to: String): Flow<List<Bus>>
}

