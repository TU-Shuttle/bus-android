package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.BusStatus
import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.BusStopBusInfo
import com.tukorea.bus.domain.repository.BusStopRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 정류장 정보를 제공하는 임시 구현체입니다.
 *
 * 실제 API가 준비되면 이 구현체를 원격 데이터 소스로 교체합니다.
 */
@Singleton
class BusStopRepositoryImpl @Inject constructor() : BusStopRepository {

    private val dummyBusStops = listOf(
        BusStop("jeongwang_station", "정왕역 탑승장소", 37.33949, 126.7326, "1캠퍼스 → 정왕역", listOf("정왕역")),
        BusStop("campus2_direction_after_10", "제2캠퍼스 방향 탑승장소", 37.33862, 126.7340, "1캠퍼스 → 2캠퍼스 (오전 10시 이후)", listOf("2캠퍼스"), operatingTimeStart = "10:00"),
        BusStop("campus2_main", "제2캠퍼스", 37.32802, 126.6884, "2캠퍼스 → 1캠퍼스 → 정왕역", listOf("1캠퍼스", "정왕역"), operatingTimeEnd = "09:30"),
        BusStop("campus1_to_campus2", "1캠퍼스", 37.34148, 126.7308, "1캠퍼스 → 2캠퍼스 (오전 9시 30분까지)", listOf("2캠퍼스"), operatingTimeEnd = "09:30"),
        BusStop("jeongwang_to_main_before_5pm", "정왕역 → 본교 (오후 5시 전)", 37.35187, 126.7415, "정왕역 → 본교", listOf("본교"), operatingTimeEnd = "17:00"),
        BusStop("jeongwang_to_main_after_5pm", "정왕역 → 본교 (오후 5시 후)", 37.35112, 126.7415, "정왕역 → 본교", listOf("본교"), operatingTimeStart = "17:00"),
        BusStop("jeongwang_to_main_to_campus2", "정왕역 → 본교 → 2캠", 37.35079, 126.7430, "정왕역 → 본교 → 2캠퍼스", listOf("본교", "2캠퍼스")),
    )

    private val dummyBusesForStop = mapOf(
        "jeongwang_station" to listOf(
            BusStopBusInfo("A노선", BusStatus.WAITING, "10:00", "정왕역"),
            BusStopBusInfo("B노선", BusStatus.FINISHED, "09:45", "정왕역"),
            BusStopBusInfo("순환", BusStatus.WAITING, "10:15", "정왕역"),
        ),
        "campus2_direction_after_10" to listOf(
            BusStopBusInfo("A노선", BusStatus.WAITING, "14:00", "2캠퍼스"),
            BusStopBusInfo("B노선", BusStatus.WAITING, "14:30", "2캠퍼스"),
        ),
        "campus2_main" to listOf(
            BusStopBusInfo("A노선", BusStatus.FINISHED, "08:00", "1캠퍼스"),
            BusStopBusInfo("B노선", BusStatus.WAITING, "09:00", "1캠퍼스"),
            BusStopBusInfo("순환", BusStatus.WAITING, "09:30", "정왕역"),
        ),
        "campus1_to_campus2" to listOf(
            BusStopBusInfo("A노선", BusStatus.WAITING, "08:30", "2캠퍼스"),
            BusStopBusInfo("B노선", BusStatus.FINISHED, "08:00", "2캠퍼스"),
        ),
        "jeongwang_to_main_before_5pm" to listOf(
            BusStopBusInfo("C노선", BusStatus.WAITING, "14:00", "본교"),
            BusStopBusInfo("D노선", BusStatus.WAITING, "14:45", "본교"),
        ),
        "jeongwang_to_main_after_5pm" to listOf(
            BusStopBusInfo("C노선", BusStatus.WAITING, "18:00", "본교"),
            BusStopBusInfo("E노선", BusStatus.WAITING, "18:30", "본교"),
        ),
        "jeongwang_to_main_to_campus2" to listOf(
            BusStopBusInfo("F노선", BusStatus.WAITING, "12:00", "본교"),
            BusStopBusInfo("G노선", BusStatus.WAITING, "12:45", "2캠퍼스"),
        ),
    )

    override suspend fun getAllBusStops(): List<BusStop> = dummyBusStops

    override suspend fun getOperatingBusStops(destination: String, currentTimeMinutes: Int): List<BusStop> =
        dummyBusStops.filter { busStop ->
            busStop.destinations.contains(destination) && busStop.isOperatingAt(currentTimeMinutes)
        }

    override suspend fun getBusesForStop(busStopId: String): List<BusStopBusInfo> =
        dummyBusesForStop[busStopId].orEmpty()

    private fun BusStop.isOperatingAt(currentTimeMinutes: Int): Boolean {
        val startTime = operatingTimeStart?.toMinutes()
        val endTime = operatingTimeEnd?.toMinutes()
        return when {
            startTime != null && endTime != null -> currentTimeMinutes in startTime..endTime
            startTime != null -> currentTimeMinutes >= startTime
            endTime != null -> currentTimeMinutes <= endTime
            else -> true
        }
    }

    private fun String.toMinutes(): Int {
        val (hours, minutes) = split(":").mapNotNull(String::toIntOrNull)
        return hours * 60 + minutes
    }
}
