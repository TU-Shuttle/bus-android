package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.RouteScheduleTemplate
import com.tukorea.bus.domain.repository.RouteScheduleRepository
import javax.inject.Inject

/**
 * 노선별 시간표 목록을 가져오는 UseCase.
 */
class GetRouteSchedulesUseCase @Inject constructor(
    private val repository: RouteScheduleRepository
) {
    /**
     * 노선별 시간표 목록을 반환합니다.
     */
    suspend operator fun invoke(): List<RouteScheduleTemplate> {
        return repository.getRouteSchedules()
    }
}

