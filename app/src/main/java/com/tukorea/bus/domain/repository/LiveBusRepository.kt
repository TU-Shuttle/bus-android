package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface LiveBusRepository {
    /** 운행 중인 버스 위치를 주기적으로 내보낸다. 수집하는 동안만 서버에 요청한다. */
    fun observeLiveBuses(): Flow<AppResult<List<LiveBus>>>
}
