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
)

data class LiveBusDto(
    val id: String?,
    val lat: Double?,
    val lng: Double?,
    val status: String?,
    val updatedAt: Long?,
)

data class RideDto(
    val id: Long?,
    val serviceDate: String?,
    val direction: String?,
    val departTime: String?,
    val dropOff: String?,
)

data class RideSummaryDto(
    val direction: String?,
    val departTime: String?,
    val riders: Long?,
    val dropOffs: Map<String, Long>?,
)

data class CreateRideBody(
    val serviceDate: String,
    val direction: String,
    val departTime: String,
    val dropOff: String,
)
