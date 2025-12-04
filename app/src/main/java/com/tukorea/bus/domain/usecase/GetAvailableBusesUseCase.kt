package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Bus
import com.tukorea.bus.domain.repository.BusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableBusesUseCase @Inject constructor(
    private val repository: BusRepository
) {
    operator fun invoke(from: String, to: String): Flow<List<Bus>> {
        return repository.getAvailableBuses(from, to)
    }
}

