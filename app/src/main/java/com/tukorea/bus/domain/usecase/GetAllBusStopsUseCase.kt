package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.repository.BusStopRepository
import javax.inject.Inject

/**
 * 모든 정류장 목록을 가져오는 UseCase
 */
class GetAllBusStopsUseCase @Inject constructor(
    private val repository: BusStopRepository
) {
    /**
     * 모든 정류장 목록을 반환합니다.
     *
     * @return 정류장 목록
     */
    suspend operator fun invoke(): List<BusStop> {
        return repository.getAllBusStops()
    }
}

