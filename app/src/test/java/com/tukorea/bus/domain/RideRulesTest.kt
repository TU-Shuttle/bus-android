package com.tukorea.bus.domain

import com.tukorea.bus.domain.error.AppError
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideSummary
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.repository.RideRepository
import com.tukorea.bus.domain.usecase.ReserveRideUseCase
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RideRulesTest {

    @Test
    fun `경유_노선은_본교_서문에서도_내릴_수_있다`() {
        assertEquals(
            listOf(ShuttleStop.CAMPUS1_WEST_GATE, ShuttleStop.CAMPUS2),
            ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1.dropOffOptions,
        )
        assertEquals(listOf(ShuttleStop.CAMPUS1), ShuttleDirection.STATION_TO_CAMPUS1.dropOffOptions)
    }

    @Test
    fun `갈_수_없는_하차지는_서버에_보내지_않고_거절한다`() = runBlocking {
        val repository = FakeRideRepository()
        val useCase = ReserveRideUseCase(repository)
        val target = RideTarget("2026-11-06", ShuttleDirection.STATION_TO_CAMPUS1, 600)

        val result = useCase(target, ShuttleStop.CAMPUS2)

        assertEquals(Result.Error(AppError.Rejected), result)
        assertFalse(repository.called)
    }

    @Test
    fun `갈_수_있는_하차지면_저장소에_요청한다`() = runBlocking {
        val repository = FakeRideRepository()
        val target = RideTarget("2026-11-06", ShuttleDirection.STATION_TO_CAMPUS1, 600)

        val result = ReserveRideUseCase(repository)(target, ShuttleStop.CAMPUS1)

        assertTrue(result is Result.Success)
        assertTrue(repository.called)
    }

    @Test
    fun `시간표_적용기간_밖인지_판단한다`() {
        val timetable = Timetable("", "2026-09-01", "2026-12-22", null, emptyList())
        assertTrue(timetable.isValidOn("2026-11-06"))
        assertFalse(timetable.isValidOn("2026-12-23"))
        assertFalse(timetable.isValidOn("2026-08-31"))
    }

    private class FakeRideRepository : RideRepository {
        var called = false
        override val myRides: Flow<List<RideIntent>> = emptyFlow()
        override suspend fun refreshMine(): AppResult<Unit> = Result.Success(Unit)
        override suspend fun reserve(target: RideTarget, dropOff: ShuttleStop): AppResult<RideIntent> {
            called = true
            return Result.Success(RideIntent(1, target.serviceDate, target.direction, target.departMinute, dropOff))
        }
        override suspend fun cancel(rideId: Long): AppResult<Unit> = Result.Success(Unit)
        override suspend fun summary(serviceDate: String): AppResult<List<RideSummary>> = Result.Success(emptyList())
    }
}
