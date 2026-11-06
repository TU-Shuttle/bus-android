package com.tukorea.bus.data.model

/** 서버 공통 응답 {"data": ..., "message": ..., "status": ...}. */
data class ApiEnvelope<T>(
    val data: T?,
)

data class TimetableDto(
    val title: String?,
    val validFrom: String?,
    val validTo: String?,
    val fetchedAt: String?,
    val schedules: List<ScheduleDto>?,
)

data class ScheduleDto(
    val dayType: String?,
    val direction: String?,
    val departTime: String?,
    val endTime: String?,
    val note: String?,
    /** "MONDAY" … 요일별 운행일 때만 온다. */
    val daysOfWeek: List<String>? = null,
)

data class LiveBusDto(
    val id: String?,
    val lat: Double?,
    val lng: Double?,
    val status: String?,
    /** 기사가 고른 노선. 예전 기사 앱이면 없다. */
    val direction: String? = null,
    val updatedAt: Long?,
)
