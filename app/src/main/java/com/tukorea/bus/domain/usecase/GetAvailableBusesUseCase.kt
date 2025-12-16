package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Bus
import com.tukorea.bus.domain.repository.BusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 출발지와 도착지에 따른 이용 가능한 버스 목록을 가져오는 UseCase.
 */
class GetAvailableBusesUseCase @Inject constructor(
    private val repository: BusRepository
) {
    /**
     * 출발지와 도착지에 따른 이용 가능한 버스 목록을 반환합니다.
     *
     * @param from 출발지 이름
     * @param to 도착지 이름
     * @return 이용 가능한 버스 목록을 포함한 Flow
     */
    operator fun invoke(from: String, to: String): Flow<List<Bus>> {
        return repository.getAvailableBuses(from, to)
    }
}

