package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Station
import com.tukorea.bus.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStationsByRouteUseCase @Inject constructor(
    private val repository: StationRepository
) {
    operator fun invoke(route: String): Flow<List<Station>> {
        return repository.getStationsByRoute(route)
    }
}

