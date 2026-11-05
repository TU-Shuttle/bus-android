package com.tukorea.bus.ui.rides

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.usecase.CancelRideUseCase
import com.tukorea.bus.domain.usecase.GetServiceMomentUseCase
import com.tukorea.bus.domain.usecase.ObserveMyRidesUseCase
import com.tukorea.bus.domain.usecase.RefreshMyRidesUseCase
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyRidesUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val today: String = "",
    val rides: List<RideIntent> = emptyList(),
    val cancellingIds: Set<Long> = emptySet(),
    @StringRes val messageRes: Int? = null,
)

@HiltViewModel
class MyRidesViewModel @Inject constructor(
    observeMyRides: ObserveMyRidesUseCase,
    private val refreshMyRides: RefreshMyRidesUseCase,
    private val cancelRide: CancelRideUseCase,
    private val getMoment: GetServiceMomentUseCase,
) : ViewModel() {

    private val local = MutableStateFlow(MyRidesUiState())

    val uiState: StateFlow<MyRidesUiState> =
        combine(local, observeMyRides()) { local, rides ->
            local.copy(rides = rides, today = getMoment().isoDate)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyRidesUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            local.update { it.copy(isLoading = true) }
            val result = refreshMyRides()
            local.update { it.copy(isLoading = false, loadFailed = result is Result.Error) }
        }
    }

    fun cancel(rideId: Long) {
        viewModelScope.launch {
            local.update { it.copy(cancellingIds = it.cancellingIds + rideId) }
            val result = cancelRide(rideId)
            local.update {
                it.copy(
                    cancellingIds = it.cancellingIds - rideId,
                    messageRes = when (result) {
                        is Result.Success -> R.string.ride_cancelled_message
                        is Result.Error -> result.error.messageRes()
                    },
                )
            }
        }
    }

    fun consumeMessage() {
        local.update { it.copy(messageRes = null) }
    }
}
