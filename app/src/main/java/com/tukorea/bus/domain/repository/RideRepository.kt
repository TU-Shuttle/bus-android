package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideSummary
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface RideRepository {
    /** 내 탑승 예정(오늘 이후). 등록·취소하면 바로 반영된다. */
    val myRides: Flow<List<RideIntent>>

    suspend fun refreshMine(): AppResult<Unit>

    suspend fun reserve(target: RideTarget, dropOff: ShuttleStop): AppResult<RideIntent>

    suspend fun cancel(rideId: Long): AppResult<Unit>

    suspend fun summary(serviceDate: String): AppResult<List<RideSummary>>
}
