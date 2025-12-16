package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 모든 알림을 읽음 처리하는 UseCase.
 */
class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * 모든 알림을 읽음 상태로 표시합니다.
     */
    suspend operator fun invoke() {
        repository.markAllAsRead()
    }
}

