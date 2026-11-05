package com.tukorea.bus.domain.model

/** 한 방향의 '지금부터' 운행 정보. */
sealed interface UpcomingDepartures {
    /** 오늘(요일)은 이 방향 운행이 없다. */
    data object NoService : UpcomingDepartures

    /** 오늘 운행이 모두 끝났다. */
    data class Finished(val firstDeparture: Departure) : UpcomingDepartures

    /**
     * @param onDemandUntil 지금 수시운행 구간이면 그 종료 시각(분), 아니면 null
     * @param next 앞으로의 정시 출발(가까운 순)
     */
    data class Scheduled(
        val onDemandUntil: Int?,
        val next: List<Departure>,
    ) : UpcomingDepartures
}
