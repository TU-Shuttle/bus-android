package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 모든 노선 목록을 가져오는 UseCase.
 */
class GetAllRoutesUseCase @Inject constructor(
    private val repository: StationRepository
) {
    /**
     * 사용 가능한 모든 노선 목록을 반환합니다.
     *
     * @return 노선 이름 목록을 포함한 Flow
     */
    operator fun invoke(): Flow<List<String>> {
        return repository.getAllRoutes()
    }
}

