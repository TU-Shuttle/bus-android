package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Notification
import com.tukorea.bus.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 모든 알림 목록을 가져오는 UseCase.
 */
class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /**
     * 저장된 모든 알림 목록을 반환합니다.
     *
     * @return 알림 목록을 포함한 Flow
     */
    operator fun invoke(): Flow<List<Notification>> {
        return repository.getAllNotifications()
    }
}

