package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Int) {
        repository.markAsRead(notificationId)
    }
}

