package com.tukorea.bus.domain.model

enum class LiveBusStatus { RUNNING, WAITING, STOP }

data class LiveBus(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val status: LiveBusStatus,
    val updatedAtMillis: Long,
    /** 기사가 고른 운행 노선. 모르면 null(어느 노선에서든 보여 준다). */
    val direction: ShuttleDirection? = null,
) {
    fun isOn(selected: ShuttleDirection?): Boolean = direction == null || selected == null || direction == selected

    /** 기사 앱에서 정한 호차 번호("bus-3" → 3). 형식이 다르면 null. */
    val number: Int? get() = NUMBER_ID.matchEntire(id)?.groupValues?.get(1)?.toIntOrNull()

    private companion object {
        val NUMBER_ID = Regex("bus-0*(\\d{1,2})")
    }
}
