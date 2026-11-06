package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.repository.LiveBusRepository
import com.tukorea.bus.domain.repository.TimeProvider
import com.tukorea.bus.domain.repository.TimetableRepository
import com.tukorea.bus.domain.util.AppResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveTimetableUseCase @Inject constructor(
    private val repository: TimetableRepository,
) {
    operator fun invoke(): Flow<Timetable?> = repository.timetable
}

class RefreshTimetableUseCase @Inject constructor(
    private val repository: TimetableRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refresh()
}

class ObserveLiveBusesUseCase @Inject constructor(
    private val repository: LiveBusRepository,
) {
    operator fun invoke(): Flow<AppResult<List<LiveBus>>> = repository.observeLiveBuses()
}

/** 남은 시간 표시가 갱신되도록 현재 시각을 주기적으로 내보낸다. */
class ObserveServiceMomentUseCase @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(): Flow<ServiceMoment> = flow {
        while (true) {
            emit(timeProvider.now())
            delay(TICK_MS)
        }
    }

    private companion object {
        const val TICK_MS = 15_000L
    }
}

class GetServiceMomentUseCase @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(): ServiceMoment = timeProvider.now()
}
