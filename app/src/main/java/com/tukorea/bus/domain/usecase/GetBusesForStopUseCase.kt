package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.BusStopBusInfo
import com.tukorea.bus.domain.repository.BusStopRepository
import javax.inject.Inject

/**
 * 특정 정류장의 버스 정보를 가져오는 UseCase
 */
class GetBusesForStopUseCase @Inject constructor(
    private val repository: BusStopRepository
) {
    /**
     * 특정 정류장 ID에 대한 버스 정보를 반환합니다.
     *
     * @param busStopId 정류장 ID
     * @return 정류장의 버스 정보 목록
     */
    suspend operator fun invoke(busStopId: String): List<BusStopBusInfo> {
        return repository.getBusesForStop(busStopId)
    }
}

