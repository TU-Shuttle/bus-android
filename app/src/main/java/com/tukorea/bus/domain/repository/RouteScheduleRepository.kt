package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.RouteScheduleTemplate

/**
 * 노선별 시간표 데이터를 제공하는 리포지토리입니다.
 * 현재는 하드코딩된 임시 데이터 구현체를 사용하지만,
 * 나중에 백엔드 API로 교체하기 쉽도록 인터페이스로 분리해 둡니다.
 */
interface RouteScheduleRepository {

    /**
     * 노선별 시간표 목록을 반환합니다.
     */
    suspend fun getRouteSchedules(): List<RouteScheduleTemplate>
}
