package com.tukorea.bus.ui.home

import com.tukorea.bus.domain.model.Departure
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
) {
    val nextDeparture: Departure?
        get() = (upcoming as? UpcomingDepartures.Scheduled)?.next?.firstOrNull()

    val runningCount: Int get() = liveBuses.count { it.status == LiveBusStatus.RUNNING }
    val waitingCount: Int get() = liveBuses.count { it.status == LiveBusStatus.WAITING }
}
