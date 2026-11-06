package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.toDomain
import com.tukorea.bus.data.util.NetworkErrorHandler
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.repository.LiveBusRepository
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 버스 위치를 5초마다 조회한다. 서버는 SSE(/api/bus/location/stream)도 제공하지만,
 * 화면에 보일 때만 짧게 조회하는 편이 배터리·재연결 처리 면에서 단순해 폴링을 쓴다.
 */
@Singleton
class LiveBusRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : LiveBusRepository {

    override fun observeLiveBuses(): Flow<AppResult<List<LiveBus>>> = flow {
        while (true) {
            val result = NetworkErrorHandler.handleNetworkRequest {
                api.getLiveBuses().mapNotNull { it.toDomain() }
            }
            emit(result)
            delay(if (result is Result.Success) POLL_MS else RETRY_MS)
        }
    }

    private companion object {
        const val POLL_MS = 2_000L
        const val RETRY_MS = 15_000L
    }
}
