package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.BusStopBusInfo

/**
 * 정류장 정보를 제공하는 Repository 인터페이스
 * Domain 레이어에서 사용하며, Data 레이어의 구현체와 분리됩니다.
 */
interface BusStopRepository {
    /**
     * 모든 정류장 목록을 반환합니다.
     *
     * @return 정류장 목록
     */
    suspend fun getAllBusStops(): List<BusStop>
    
    /**
     * 특정 목적지로 가며 현재 시간에 운행 중인 정류장만 필터링하여 반환합니다.
     *
     * @param destination 목적지 이름 (예: "정왕역", "1캠퍼스", "2캠퍼스")
     * @param currentTimeMinutes 현재 시간 (분 단위, 예: 10시 30분 = 630)
     * @return 해당 목적지로 가며 현재 운행 중인 정류장 목록
     */
    suspend fun getOperatingBusStops(destination: String, currentTimeMinutes: Int): List<BusStop>
    
    /**
     * 특정 정류장 ID에 대한 버스 정보를 반환합니다.
     *
     * @param busStopId 정류장 ID
     * @return 정류장의 버스 정보 목록
     */
    suspend fun getBusesForStop(busStopId: String): List<BusStopBusInfo>
}

