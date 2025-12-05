package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: Int)
    suspend fun markAllAsRead()
}

