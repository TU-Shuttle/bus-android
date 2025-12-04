package com.tukorea.bus.ui.home

import com.tukorea.bus.domain.model.Reservation

data class HomeUiState(
    val nextReservation: Reservation? = null,
    val modalHeight: ModalHeight = ModalHeight.MID,
    val isModalVisible: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val runningBusesCount: Int = 0,
    val waitingBusesCount: Int = 0,
    val hasUnreadImportantNotice: Boolean = false,  // 읽지 않은 중요 알림 존재 여부
    val firstUnreadImportantNoticeId: Int? = null  // 첫 번째 읽지 않은 중요 알림 ID
)

/**
 * 모달 높이를 나타내는 enum
 * LOW: 80dp (드래그 바만 보임)
 * MID: 화면의 40% (중간 높이, 시작 상태)
 * HIGH: 화면의 75% (최대 높이, 상단 바에 안 가림)
 */
enum class ModalHeight {
    LOW,
    MID,
    HIGH
}
