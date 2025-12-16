package com.tukorea.bus.domain.usecase

import javax.inject.Inject

/**
 * 요일 목록을 정렬하는 UseCase.
 */
class SortDaysUseCase @Inject constructor() {
    private val dayOrder = listOf("월", "화", "수", "목", "금", "토", "일")

    /**
     * 요일 목록을 월요일부터 일요일 순서로 정렬합니다.
     *
     * @param days 정렬할 요일 목록
     * @return 정렬된 요일 목록
     */
    operator fun invoke(days: List<String>): List<String> {
        return days.sortedBy { dayOrder.indexOf(it) }
    }
}

