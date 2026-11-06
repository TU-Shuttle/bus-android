package com.tukorea.bus.data.repository

import android.os.SystemClock
import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.toDomain
import com.tukorea.bus.data.remote.LocationStreamSource
import com.tukorea.bus.data.util.NetworkErrorHandler
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.repository.LiveBusRepository
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 버스 위치를 실시간으로 받는다.
 * 1) 연결할 때 현재 위치 목록을 한 번 조회하고 2) 이후엔 SSE로 바뀐 위치만 받아 목록을 고친다.
 * SSE는 사라진 버스를 알려 주지 않으므로, 서버 기준(60초)보다 오래 소식이 없는 버스는 여기서 지운다.
 * 스트림이 끊기면 잠시 뒤 다시 1)부터 한다. 화면이 구독하는 동안에만 연결한다.
 */
@Singleton
class LiveBusRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val stream: LocationStreamSource,
) : LiveBusRepository {

    private sealed interface Signal {
        data class Update(val bus: LiveBus) : Signal
        data object Tick : Signal
    }

    override fun observeLiveBuses(): Flow<AppResult<List<LiveBus>>> = flow {
        while (currentCoroutineContext().isActive) {
            val snapshot = NetworkErrorHandler.handleNetworkRequest {
                api.getLiveBuses().mapNotNull { it.toDomain() }
            }
            if (snapshot !is Result.Success) {
                emit(snapshot)
                delay(RETRY_MS)
                continue
            }

            val buses = snapshot.data.associateBy { it.id }.toMutableMap()
            val seenAt = buses.keys.associateWith { SystemClock.elapsedRealtime() }.toMutableMap()
            emit(Result.Success(buses.sorted()))

            try {
                streamWithTicks()
                    .collect { signal ->
                        val now = SystemClock.elapsedRealtime()
                        val changed = when (signal) {
                            is Signal.Update -> {
                                buses[signal.bus.id] = signal.bus
                                seenAt[signal.bus.id] = now
                                true
                            }
                            Signal.Tick -> {
                                val stale = seenAt.filterValues { now - it > STALE_MS }.keys
                                stale.forEach { buses.remove(it); seenAt.remove(it) }
                                stale.isNotEmpty()
                            }
                        }
                        if (changed) emit(Result.Success(buses.sorted()))
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 끊김은 다음 바퀴에서 다시 연결한다. 그 사이 화면엔 마지막 위치가 남는다.
            }
            delay(RECONNECT_MS)
        }
    }

    /** 스트림의 위치 + 오래된 버스를 지우기 위한 주기 신호. 스트림이 끝나면 함께 끝난다. */
    private fun streamWithTicks(): Flow<Signal> = channelFlow {
        val ticker = launch {
            while (true) {
                delay(PRUNE_EVERY_MS)
                send(Signal.Tick)
            }
        }
        stream.stream().collect { dto -> dto.toDomain()?.let { send(Signal.Update(it)) } }
        ticker.cancel()
    }

    private fun Map<String, LiveBus>.sorted(): List<LiveBus> = values.sortedBy { it.id }

    private companion object {
        const val RETRY_MS = 15_000L
        const val RECONNECT_MS = 3_000L
        const val PRUNE_EVERY_MS = 5_000L

        /** 서버의 shuttle.realtime.stale-after(기본 60초)와 맞춘다. */
        const val STALE_MS = 60_000L
    }
}
