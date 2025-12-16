package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Station
import com.tukorea.bus.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 노선의 정류장 목록을 가져오는 UseCase.
 */
class GetStationsByRouteUseCase @Inject constructor(
    private val repository: StationRepository
) {
    /**
     * 지정된 노선의 정류장 목록을 반환합니다.
     *
     * @param route 노선 이름
     * @return 정류장 목록을 포함한 Flow
     */
    operator fun invoke(route: String): Flow<List<Station>> {
        return repository.getStationsByRoute(route)
    }
}

