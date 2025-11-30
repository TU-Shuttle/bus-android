package com.tukorea.bus.domain.model

data class Notification(
    val id: Int,
    val type: NotificationType,
    val title: String,
    val content: String,
    val time: String,
    val read: Boolean,
    val important: Boolean
)
enum class NotificationType {
    WARNING, BUS, INFO, SUCCESS, SYSTEM
}

