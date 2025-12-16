package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 특정 알림을 읽음 처리하는 UseCase.
 */
class MarkNotificationAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * 지정된 ID의 알림을 읽음 상태로 표시합니다.
     *
     * @param notificationId 읽음 처리할 알림의 ID
     */
    suspend operator fun invoke(notificationId: Int) {
        repository.markAsRead(notificationId)
    }
}

