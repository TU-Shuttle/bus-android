package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.repository.BusStopBusInfo
import com.tukorea.bus.domain.repository.BusStopRepository
import com.tukorea.bus.domain.repository.BusStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BusStopRepository의 구현체
 * 현재는 더미 데이터를 반환하며, 향후 백엔드 API 연동 시 DataSource를 주입받아 사용합니다.
 */
@Singleton
class BusStopRepositoryImpl @Inject constructor() : BusStopRepository {
    private val dummyBusStops = listOf(
        BusStop(
            id = "jeongwang_station",
            name = "정왕역 탑승장소",
            latitude = 37.33949,
            longitude = 126.7326,
            description = "1캠퍼스 → 정왕역",
            destinations = listOf("정왕역")
        ),
        BusStop(
            id = "campus2_direction_after_10",
            name = "제2캠퍼스 방향 탑승장소",
            latitude = 37.33862,
            longitude = 126.7340,
            description = "1캠퍼스 → 2캠퍼스 (오전 10시 이후)",
            destinations = listOf("2캠퍼스"),
            operatingTimeStart = "10:00"
        ),
        BusStop(
            id = "campus2_main",
            name = "제2캠퍼스",
            latitude = 37.32802,
            longitude = 126.6884,
            description = "2캠퍼스 → 1캠퍼스 → 정왕역",
            destinations = listOf("1캠퍼스", "정왕역"),
            operatingTimeEnd = "09:30"
        ),
        BusStop(
            id = "campus1_to_campus2",
            name = "1캠퍼스",
            latitude = 37.34148,
            longitude = 126.7308,
            description = "1캠퍼스 → 2캠퍼스 (오전 9시 30분까지)",
            destinations = listOf("2캠퍼스"),
            operatingTimeEnd = "09:30"
        ),
        BusStop(
            id = "jeongwang_to_main_before_5pm",
            name = "정왕역 → 본교 (오후 5시 전)",
            latitude = 37.35187,
            longitude = 126.7415,
            description = "정왕역 → 본교",
            destinations = listOf("본교"),
            operatingTimeEnd = "17:00"
        ),
        BusStop(
            id = "jeongwang_to_main_after_5pm",
            name = "정왕역 → 본교 (오후 5시 후)",
            latitude = 37.35112,
            longitude = 126.7415,
            description = "정왕역 → 본교",
            destinations = listOf("본교"),
            operatingTimeStart = "17:00"
        ),
        BusStop(
            id = "jeongwang_to_main_to_campus2",
            name = "정왕역 → 본교 → 2캠",
            latitude = 37.35079,
            longitude = 126.7430,
            description = "정왕역 → 본교 → 2캠퍼스",
            destinations = listOf("본교", "2캠퍼스")
        )
    )

    private val dummyBusesForStop = mapOf(
        "jeongwang_station" to listOf(
            BusStopBusInfo("A노선", BusStatus.WAITING, "10:00", "정왕역"),
            BusStopBusInfo("B노선", BusStatus.DEPARTED, "09:45", "정왕역"),
            BusStopBusInfo("순환", BusStatus.WAITING, "10:15", "정왕역")
        ),
        "campus2_direction_after_10" to listOf(
            BusStopBusInfo("A노선", BusStatus.WAITING, "14:00", "2캠퍼스"),
            BusStopBusInfo("B노선", BusStatus.WAITING, "14:30", "2캠퍼스")
        ),
        "campus2_main" to listOf(
            BusStopBusInfo("A노선", BusStatus.DEPARTED, "08:00", "1캠퍼스"),
            BusStopBusInfo("B노선", BusStatus.WAITING, "09:00", "1캠퍼스"),
            BusStopBusInfo("순환", BusStatus.WAITING, "09:30", "정왕역")
        ),
        "campus1_to_campus2" to listOf(
            BusStopBusInfo("A노선", BusStatus.WAITING, "08:30", "2캠퍼스"),
            BusStopBusInfo("B노선", BusStatus.DEPARTED, "08:00", "2캠퍼스")
        ),
        "jeongwang_to_main_before_5pm" to listOf(
            BusStopBusInfo("C노선", BusStatus.WAITING, "14:00", "본교"),
            BusStopBusInfo("D노선", BusStatus.WAITING, "14:45", "본교")
        ),
        "jeongwang_to_main_after_5pm" to listOf(
            BusStopBusInfo("C노선", BusStatus.WAITING, "18:00", "본교"),
            BusStopBusInfo("E노선", BusStatus.WAITING, "18:30", "본교")
        ),
        "jeongwang_to_main_to_campus2" to listOf(
            BusStopBusInfo("F노선", BusStatus.WAITING, "12:00", "본교"),
            BusStopBusInfo("G노선", BusStatus.WAITING, "12:45", "2캠퍼스")
        )
    )

    // endregion

    override suspend fun getAllBusStops(): List<BusStop> {
        return dummyBusStops
    }

    override suspend fun getOperatingBusStops(destination: String, currentTimeMinutes: Int): List<BusStop> {
        return dummyBusStops.filter { busStop ->
            val goesToDestination = busStop.destinations.contains(destination)

            val startTime = busStop.operatingTimeStart?.let { timeStringToMinutes(it) }
            val endTime = busStop.operatingTimeEnd?.let { timeStringToMinutes(it) }

            val isOperating = when {
                startTime != null && endTime == null -> currentTimeMinutes >= startTime
                startTime == null && endTime != null -> currentTimeMinutes <= endTime
                startTime != null && endTime != null -> currentTimeMinutes in startTime..endTime
                else -> true
            }

            goesToDestination && isOperating
        }
    }

    override suspend fun getBusesForStop(busStopId: String): List<BusStopBusInfo> {
        return dummyBusesForStop[busStopId] ?: emptyList()
    }

    /**
     * 시간 문자열(HH:mm)을 분 단위로 변환합니다.
     */
    private fun timeStringToMinutes(timeString: String): Int {
        val parts = timeString.split(":")
        if (parts.size != 2) return 0
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0
        return hours * 60 + minutes
    }
}

