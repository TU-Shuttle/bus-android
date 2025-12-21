package com.tukorea.bus.data.datasource

import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.repository.BusStopBusInfo
import com.tukorea.bus.domain.repository.BusStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 정류장 정보를 제공하는 데이터 소스
 * 실제 프로덕션에서는 API 또는 로컬 DB에서 데이터를 가져와야 합니다.
 */
@Singleton
class BusStopDataSource @Inject constructor() {

    /**
     * 모든 정류장 목록을 반환합니다.
     *
     * @return 정류장 목록
     */
    fun getAllBusStops(): List<BusStop> {
        return listOf(
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
    }

    /**
     * 특정 목적지로 가며 현재 시간에 운행 중인 정류장만 필터링하여 반환합니다.
     *
     * @param destination 목적지 이름 (예: "정왕역", "1캠퍼스", "2캠퍼스")
     * @param currentTimeMinutes 현재 시간 (분 단위, 예: 10시 30분 = 630)
     * @return 해당 목적지로 가며 현재 운행 중인 정류장 목록
     */
    fun getOperatingBusStops(destination: String, currentTimeMinutes: Int): List<BusStop> {
        return getAllBusStops().filter { busStop ->
            // 1. 목적지 필터링: 이 정류장이 선택한 목적지로 가는지 확인
            val goesToDestination = busStop.destinations.contains(destination)

            // 2. 운행 시간 필터링
            val startTime = busStop.operatingTimeStart?.let { timeStringToMinutes(it) }
            val endTime = busStop.operatingTimeEnd?.let { timeStringToMinutes(it) }

            val isOperating = when {
                // 시작 시간만 있는 경우: 시작 시간 이후면 운행
                startTime != null && endTime == null -> currentTimeMinutes >= startTime
                // 종료 시간만 있는 경우: 종료 시간 이전이면 운행
                startTime == null && endTime != null -> currentTimeMinutes <= endTime
                // 둘 다 있는 경우: 시작~종료 사이면 운행
                startTime != null && endTime != null -> currentTimeMinutes in startTime..endTime
                // 둘 다 없는 경우: 항상 운행
                else -> true
            }

            // 목적지도 맞고 운행 시간도 맞는 정류장만 반환
            goesToDestination && isOperating
        }
    }

    /**
     * 시간 문자열(HH:mm)을 분 단위로 변환합니다.
     *
     * @param timeString 시간 문자열 (예: "10:30")
     * @return 분 단위 시간 (예: 630)
     */
    private fun timeStringToMinutes(timeString: String): Int {
        val parts = timeString.split(":")
        if (parts.size != 2) return 0
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0
        return hours * 60 + minutes
    }

    /**
     * 특정 정류장의 버스 운행 정보를 반환합니다 (더미 데이터)
     * 실제 프로덕션에서는 API에서 실시간 버스 정보를 가져와야 합니다.
     *
     * @param busStopId 정류장 ID
     * @return 해당 정류장의 버스 목록
     */
    fun getBusesForStop(busStopId: String): List<BusStopBusInfo> {
        return when (busStopId) {
            "jeongwang_station" -> listOf(
                BusStopBusInfo("A노선", BusStatus.WAITING, "10:00", "정왕역"),
                BusStopBusInfo("B노선", BusStatus.DEPARTED, "09:45", "정왕역"),
                BusStopBusInfo("순환", BusStatus.WAITING, "10:15", "정왕역")
            )
            "campus2_direction_after_10" -> listOf(
                BusStopBusInfo("A노선", BusStatus.WAITING, "14:00", "2캠퍼스"),
                BusStopBusInfo("B노선", BusStatus.WAITING, "14:30", "2캠퍼스")
            )
            "campus2_main" -> listOf(
                BusStopBusInfo("A노선", BusStatus.DEPARTED, "08:00", "1캠퍼스"),
                BusStopBusInfo("B노선", BusStatus.WAITING, "09:00", "1캠퍼스"),
                BusStopBusInfo("순환", BusStatus.WAITING, "09:30", "정왕역")
            )
            "campus1_to_campus2" -> listOf(
                BusStopBusInfo("A노선", BusStatus.WAITING, "08:30", "2캠퍼스"),
                BusStopBusInfo("B노선", BusStatus.DEPARTED, "08:00", "2캠퍼스")
            )
            "jeongwang_to_main_before_5pm" -> listOf(
                BusStopBusInfo("C노선", BusStatus.WAITING, "14:00", "본교"),
                BusStopBusInfo("D노선", BusStatus.WAITING, "14:45", "본교")
            )
            "jeongwang_to_main_after_5pm" -> listOf(
                BusStopBusInfo("C노선", BusStatus.WAITING, "18:00", "본교"),
                BusStopBusInfo("E노선", BusStatus.WAITING, "18:30", "본교")
            )
            "jeongwang_to_main_to_campus2" -> listOf(
                BusStopBusInfo("F노선", BusStatus.WAITING, "12:00", "본교"),
                BusStopBusInfo("G노선", BusStatus.WAITING, "12:45", "2캠퍼스")
            )
            else -> emptyList()
        }
    }
}
