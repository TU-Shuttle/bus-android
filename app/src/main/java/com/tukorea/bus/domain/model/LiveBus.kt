package com.tukorea.bus.domain.model

enum class LiveBusStatus { RUNNING, WAITING, STOP }

data class LiveBus(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val status: LiveBusStatus,
    val updatedAtMillis: Long,
)
