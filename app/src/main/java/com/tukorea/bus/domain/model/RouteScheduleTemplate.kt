package com.tukorea.bus.domain.model

/**
 * 노선별 시간표에 대한 도메인 모델입니다.
 * 실제 UI 색상/아이콘 등은 포함하지 않고, 노선 이름과 시간 정보만 담습니다.
 */
data class RouteScheduleTemplate(
    val name: String,
    val morningTimes: List<String>,
    val afternoonTimes: List<String>
)


