package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.Notification
import com.tukorea.bus.domain.model.NotificationType
import com.tukorea.bus.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
) : NotificationRepository {
    
    private val _notifications = MutableStateFlow(createDummyNotifications())
    private val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    override fun getAllNotifications(): Flow<List<Notification>> {
        return notifications.map { notificationList ->
            notificationList.sortedWith(
                compareBy<Notification> { it.read }
                    .thenBy { notification ->
                        when {
                            notification.time.contains("분 전") -> {
                                notification.time.replace("분 전", "").toIntOrNull() ?: Int.MAX_VALUE
                            }
                            notification.time.contains("시간 전") -> {
                                (notification.time.replace("시간 전", "").toIntOrNull() ?: Int.MAX_VALUE) * 60
                            }
                            notification.time.contains("일 전") -> {
                                (notification.time.replace("일 전", "").toIntOrNull() ?: Int.MAX_VALUE) * 1440
                            }
                            else -> Int.MAX_VALUE
                        }
                    }
            )
        }
    }
    
    override suspend fun markAsRead(notificationId: Int) {
        _notifications.value = _notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(read = true)
            } else {
                notification
            }
        }
    }
    
    override suspend fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(read = true) }
    }

    private fun createDummyNotifications(): List<Notification> {
        return listOf(
            Notification(
                id = 1,
                type = NotificationType.WARNING,
                title = "운행 변경 안내",
                content = "11월 25일(월) A노선이 일시적으로 운행 중단됩니다. B노선을 이용해 주세요.",
                time = "10분 전",
                read = false,
                important = true
            ),
            Notification(
                id = 2,
                type = NotificationType.BUS,
                title = "버스 도착 예정",
                content = "예약하신 09:00 기숙사→본관 버스가 5분 후 도착합니다.",
                time = "30분 전",
                read = false,
                important = false
            ),
            Notification(
                id = 3,
                type = NotificationType.INFO,
                title = "신규 노선 안내",
                content = "12월 1일부터 D노선(공대↔도서관)이 신설됩니다.",
                time = "1일 전",
                read = true,
                important = false
            ),
            Notification(
                id = 4,
                type = NotificationType.SUCCESS,
                title = "예약 완료",
                content = "월,수,금 09:00 반복 예약이 설정되었습니다.",
                time = "2일 전",
                read = true,
                important = false
            ),
            Notification(
                id = 5,
                type = NotificationType.SYSTEM,
                title = "앱 업데이트",
                content = "v2.1.0 업데이트가 완료되었습니다.",
                time = "3일 전",
                read = true,
                important = false
            )
        )
    }
}

