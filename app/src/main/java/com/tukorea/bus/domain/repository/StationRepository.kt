package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Station
import kotlinx.coroutines.flow.Flow

interface StationRepository {
    fun getStationsByRoute(route: String): Flow<List<Station>>
    fun getAllRoutes(): Flow<List<String>>
}

