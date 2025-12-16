package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 캘린더에서 사용할 요일 목록을 가져오는 UseCase.
 */
class GetDaysUseCase @Inject constructor(
    private val repository: CalendarRepository
) {
    /**
     * 요일 목록을 반환합니다.
     */
    operator fun invoke(): Flow<List<String>> {
        return repository.getDays()
    }
}

