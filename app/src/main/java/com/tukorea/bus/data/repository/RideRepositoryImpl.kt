package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.local.LocalStore
import com.tukorea.bus.data.model.toBody
import com.tukorea.bus.data.model.toDomain
import com.tukorea.bus.data.util.NetworkErrorHandler
import com.tukorea.bus.domain.error.AppError
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideSummary
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.repository.RideRepository
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val localStore: LocalStore,
) : RideRepository {

    private val rides = MutableStateFlow<List<RideIntent>>(emptyList())
    override val myRides: Flow<List<RideIntent>> = rides.asStateFlow()

    override suspend fun refreshMine(): AppResult<Unit> {
        val result = NetworkErrorHandler.handleNetworkRequest {
            api.getMyRides(localStore.deviceId()).data.orEmpty().mapNotNull { it.toDomain() }
        }
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                rides.value = result.data.sorted()
                Result.Success(Unit)
            }
        }
    }

    override suspend fun reserve(target: RideTarget, dropOff: ShuttleStop): AppResult<RideIntent> {
        val result = NetworkErrorHandler.handleNetworkRequest {
            api.reserveRide(localStore.deviceId(), target.toBody(dropOff)).data?.toDomain()
        }
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                val ride = result.data ?: return Result.Error(AppError.UnknownError)
                rides.update { current -> (current.filterNot { it.id == ride.id } + ride).sorted() }
                Result.Success(ride)
            }
        }
    }

    override suspend fun cancel(rideId: Long): AppResult<Unit> {
        val result = NetworkErrorHandler.handleNetworkRequest {
            api.cancelRide(localStore.deviceId(), rideId)
        }
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                rides.update { current -> current.filterNot { it.id == rideId } }
                Result.Success(Unit)
            }
        }
    }

    override suspend fun summary(serviceDate: String): AppResult<List<RideSummary>> =
        NetworkErrorHandler.handleNetworkRequest {
            api.getRideSummary(serviceDate).data.orEmpty().mapNotNull { it.toDomain() }
        }

    private fun List<RideIntent>.sorted(): List<RideIntent> =
        sortedWith(compareBy({ it.serviceDate }, { it.departMinute }))
}
