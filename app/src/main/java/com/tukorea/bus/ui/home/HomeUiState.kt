package com.tukorea.bus.ui.home

import com.tukorea.bus.domain.model.BusTracking
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ShuttleRoute
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.LiveBusStatus
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.UpcomingDepartures

data class HomeUiState(
    val isLoading: Boolean = true,
    /** 시간표를 한 번도 받지 못했고(저장본 없음) 새로 받기도 실패함. */
    val loadFailed: Boolean = false,
    val directions: List<ShuttleDirection> = emptyList(),
    val selectedDirection: ShuttleDirection? = null,
    val upcoming: UpcomingDepartures? = null,
    val nowMinute: Int = 0,
    /** 오늘이 시간표 적용 기간 밖이면 "9.1 – 12.22" 같은 기간 표시. */
    val outOfPeriod: String? = null,
    val liveBuses: List<LiveBus> = emptyList(),
    val liveUnavailable: Boolean = false,
    /** 고른 노선의 도로 경로. 지도에 선으로 그린다. */
    val route: ShuttleRoute? = null,
    /** 버스별 진행 방향(°). 지도 마커의 방향 화살표에 쓴다. */
    val headings: Map<String, Double> = emptyMap(),
    /** 지도에서 누른 버스. 있으면 시트가 그 버스의 운행 정보로 바뀌고 지도가 버스를 따라간다. */
    val selectedBus: BusTracking? = null,
) {
    val nextDeparture: Departure?
        get() = (upcoming as? UpcomingDepartures.Scheduled)?.next?.firstOrNull()

    val runningCount: Int get() = liveBuses.count { it.status == LiveBusStatus.RUNNING }
    val waitingCount: Int get() = liveBuses.count { it.status == LiveBusStatus.WAITING }
}
