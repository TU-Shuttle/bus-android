package com.tukorea.bus.ui.ride

import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.usecase.ReserveRideUseCase
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class RideSheetState(
    val target: RideTarget,
    val options: List<ShuttleStop>,
    val selected: ShuttleStop,
    val isSubmitting: Boolean = false,
)

/**
 * '이 셔틀 탈게요' 시트의 상태와 제출. 홈과 시간표 ViewModel이 각자 하나씩 가진다.
 * 하차지가 하나뿐이어도 시트를 보여 준다(무엇을 남기는지 확인하도록).
 */
class RideReservation @Inject constructor(
    private val reserveRide: ReserveRideUseCase,
) {
    private val _sheet = MutableStateFlow<RideSheetState?>(null)
    val sheet: StateFlow<RideSheetState?> = _sheet.asStateFlow()

    fun open(target: RideTarget) {
        val options = target.direction.dropOffOptions
        _sheet.value = RideSheetState(target = target, options = options, selected = options.last())
    }

    fun select(stop: ShuttleStop) {
        _sheet.update { it?.copy(selected = stop) }
    }

    fun dismiss() {
        _sheet.value = null
    }

    /** 성공하면 시트를 닫는다. 진행 중이거나 시트가 없으면 null. */
    suspend fun submit(): AppResult<RideIntent>? {
        val current = _sheet.value ?: return null
        if (current.isSubmitting) return null
        _sheet.value = current.copy(isSubmitting = true)
        val result = reserveRide(current.target, current.selected)
        _sheet.value = if (result is Result.Success) null else current.copy(isSubmitting = false)
        return result
    }
}
