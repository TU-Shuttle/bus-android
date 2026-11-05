package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.util.AppResult
import javax.inject.Inject

/** 앱 시작 시 시간표를 미리 받아 둔다. 실패해도(오프라인) 저장된 시간표로 계속 쓸 수 있다. */
class InitializeAppUseCase @Inject constructor(
    private val refreshTimetable: RefreshTimetableUseCase,
) {
    suspend operator fun invoke(): AppResult<Unit> = refreshTimetable()
}
