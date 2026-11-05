package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.error.AppError
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideSummary
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.repository.RideRepository
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMyRidesUseCase @Inject constructor(
    private val repository: RideRepository,
) {
    operator fun invoke(): Flow<List<RideIntent>> = repository.myRides
}

class RefreshMyRidesUseCase @Inject constructor(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refreshMine()
}

/** 방향에서 갈 수 없는 하차지는 서버에 보내기 전에 거절한다. */
class ReserveRideUseCase @Inject constructor(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(target: RideTarget, dropOff: ShuttleStop): AppResult<RideIntent> {
        if (dropOff !in target.direction.dropOffOptions) return Result.Error(AppError.Rejected)
        return repository.reserve(target, dropOff)
    }
}

class CancelRideUseCase @Inject constructor(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(rideId: Long): AppResult<Unit> = repository.cancel(rideId)
}

class GetRideSummaryUseCase @Inject constructor(
    private val repository: RideRepository,
) {
    suspend operator fun invoke(serviceDate: String): AppResult<List<RideSummary>> =
        repository.summary(serviceDate)
}
