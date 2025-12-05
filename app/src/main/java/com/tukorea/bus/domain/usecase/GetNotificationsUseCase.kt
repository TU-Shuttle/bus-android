package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Notification
import com.tukorea.bus.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<Notification>> {
        return repository.getAllNotifications()
    }
}

