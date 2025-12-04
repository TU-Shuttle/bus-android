package com.tukorea.bus.ui.home

import com.tukorea.bus.domain.model.Reservation

data class HomeUiState(
    val nextReservation: Reservation? = null,
    val modalHeight: ModalHeight = ModalHeight.LOW,
    val isModalVisible: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val runningBusesCount: Int = 0,
    val waitingBusesCount: Int = 0
)

/**
 * 모달 높이를 나타내는 enum인데 모달 구현 아직 안해서 쓸모 없음
 */
enum class ModalHeight {
    LOW,
    MID,
    HIGH
}
