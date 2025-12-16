package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 읽지 않은 중요 알림 정보를 가져오는 UseCase.
 */
class GetUnreadImportantNotificationsUseCase @Inject constructor() {
    /**
     * 알림 목록에서 읽지 않은 중요 알림 정보를 추출합니다.
     *
     * @param notifications 전체 알림 목록
     * @return 읽지 않은 중요 알림이 있는지 여부와 첫 번째 알림 ID를 포함한 데이터 클래스
     */
    data class Result(
        val hasUnreadImportant: Boolean,
        val firstUnreadImportantId: Int?
    )

    /**
     * 알림 목록에서 읽지 않은 중요 알림을 필터링하여 결과를 반환합니다.
     *
     * @param notifications 전체 알림 목록
     * @return 읽지 않은 중요 알림 정보
     */
    operator fun invoke(notifications: List<Notification>): Result {
        val unreadImportant = notifications.filter { it.important && !it.read }
        return Result(
            hasUnreadImportant = unreadImportant.isNotEmpty(),
            firstUnreadImportantId = unreadImportant.firstOrNull()?.id
        )
    }

    /**
     * Flow 형태의 알림 목록에서 읽지 않은 중요 알림 정보를 추출합니다.
     *
     * @param notificationsFlow 전체 알림 목록 Flow
     * @return 읽지 않은 중요 알림 정보 Flow
     */
    operator fun invoke(notificationsFlow: Flow<List<Notification>>): Flow<Result> {
        return notificationsFlow.map { notifications -> invoke(notifications) }
    }
}

