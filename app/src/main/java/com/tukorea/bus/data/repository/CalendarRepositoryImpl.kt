package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 캘린더(예약 입력 폼) Repository 구현체
 *
 * TODO: PDF 시간표에 맞춰 등교/하교 시간을 업데이트하세요
 * - 등교(정왕역 출발): 08:40~10:00 수시운행 + 개별 시간
 * - 하교(학교 출발): 개별 시간표
 * - 주말 시간표 별도 관리 필요시 확장 가능
 */
@Singleton
class CalendarRepositoryImpl @Inject constructor() : CalendarRepository {

    // 등교 시간표 (정왕역 → 학교)
    private val goingToSchoolTimes = listOf(
        // TODO: PDF 시간표에 맞춰 업데이트
        "08:40", "09:00", "09:20", "09:40", "10:00",
        "13:00", "14:00", "15:00", "16:00"
    )

    // 하교 시간표 (학교 → 정왕역)
    private val leavingSchoolTimes = listOf(
        // TODO: PDF 시간표에 맞춰 업데이트
        "09:00", "10:00", "11:00", "12:00",
        "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
    )

    override fun getDays(): Flow<List<String>> {
        val operatingDays = listOf("월", "화", "수", "목", "금", "토", "일")
        return flowOf(operatingDays)
    }

    override fun getTimes(): Flow<List<String>> {
        // 기본적으로 등교와 하교 시간을 합쳐서 반환
        // UI에서 scheduleType에 따라 필터링할 수 있도록 모든 시간 제공
        val allTimes = (goingToSchoolTimes + leavingSchoolTimes).distinct().sorted()
        return flowOf(allTimes)
    }

    override fun getTimesByType(scheduleType: String): Flow<List<String>> {
        return when (scheduleType) {
            "등교" -> flowOf(goingToSchoolTimes)
            "하교" -> flowOf(leavingSchoolTimes)
            else -> getTimes() // 기본값으로 모든 시간 반환
        }
    }

    override fun getTimesByRoute(from: String, to: String): Flow<List<String>> {
        return when {
            // 정왕역 -> 캠퍼스 (등교)
            from == "정왕역" && (to == "1캠퍼스" || to == "2캠퍼스") -> {
                flowOf(goingToSchoolTimes)
            }
            // 캠퍼스 -> 정왕역 (하교)
            (from == "1캠퍼스" || from == "2캠퍼스") && to == "정왕역" -> {
                flowOf(leavingSchoolTimes)
            }
            // 기타 경로는 모든 시간 제공
            else -> getTimes()
        }
    }

    override fun getLocations(): Flow<List<String>> {
        val stations = listOf("1캠퍼스", "2캠퍼스", "정왕역")
        return flowOf(stations)
    }
}

