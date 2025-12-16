package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 캘린더에서 사용할 장소(정류장) 목록을 가져오는 UseCase.
 */
class GetLocationsUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    /**
     * 장소 목록을 반환합니다.
     */
    operator fun invoke(): Flow<List<String>> {
        return repository.getLocations()
    }
}

