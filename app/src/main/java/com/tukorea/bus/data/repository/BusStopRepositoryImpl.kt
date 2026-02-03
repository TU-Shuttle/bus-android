package com.tukorea.bus.data.repository

import com.tukorea.bus.data.datasource.BusStopDataSource
import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.repository.BusStopBusInfo
import com.tukorea.bus.domain.repository.BusStopRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BusStopRepository의 구현체
 * Data 레이어에서 BusStopDataSource를 사용하여 데이터를 제공합니다.
 */
@Singleton
class BusStopRepositoryImpl @Inject constructor(
    private val dataSource: BusStopDataSource
) : BusStopRepository {
    
    override suspend fun getAllBusStops(): List<BusStop> {
        return dataSource.getAllBusStops()
    }
    
    override suspend fun getOperatingBusStops(destination: String, currentTimeMinutes: Int): List<BusStop> {
        return dataSource.getOperatingBusStops(destination, currentTimeMinutes)
    }
    
    override suspend fun getBusesForStop(busStopId: String): List<BusStopBusInfo> {
        // DataSource에서 이미 Domain 레이어의 BusStopBusInfo를 반환하므로 변환 불필요
        return dataSource.getBusesForStop(busStopId)
    }
}

