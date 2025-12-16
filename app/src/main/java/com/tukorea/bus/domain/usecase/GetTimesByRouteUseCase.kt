package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 출발지와 도착지에 따른 시간표를 가져오는 UseCase.
 */
class GetTimesByRouteUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    /**
     * 출발지와 도착지에 따른 시간표를 반환합니다.
     *
     * @param from 출발지 이름
     * @param to 도착지 이름
     */
    operator fun invoke(from: String, to: String): Flow<List<String>> {
        return repository.getTimesByRoute(from, to)
    }
}

