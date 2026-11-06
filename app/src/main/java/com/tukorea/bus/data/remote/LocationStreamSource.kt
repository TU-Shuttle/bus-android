package com.tukorea.bus.data.remote

import com.google.gson.Gson
import com.tukorea.bus.BuildConfig
import com.tukorea.bus.data.model.LiveBusDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버의 전체 버스 위치 스트림(`GET /api/bus/location/stream`, SSE)을 읽는다.
 * 서버가 20초마다 keep-alive를 보내므로 그보다 넉넉한 읽기 제한으로 끊긴 연결을 알아챈다.
 * 연결이 끝나면(서버 종료, 네트워크 끊김) Flow도 끝나거나 예외를 던진다. 다시 연결은 호출하는 쪽이 한다.
 */
@Singleton
class LocationStreamSource @Inject constructor(
    client: OkHttpClient,
    private val gson: Gson,
) {
    private val streamClient = client.newBuilder()
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /** 구독을 취소하면 요청을 끊어(call.cancel) 막혀 있던 읽기도 바로 끝난다. */
    fun stream(): Flow<LiveBusDto> = callbackFlow {
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL.trimEnd('/') + PATH)
            .header("Accept", "text/event-stream")
            .build()
        val call = streamClient.newCall(request)
        call.enqueue(
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    close(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            close(IOException("SSE ${response.code}"))
                            return
                        }
                        try {
                            readEvents(response)
                            close()
                        } catch (e: IOException) {
                            close(e)
                        }
                    }
                }

                private fun readEvents(response: Response) {
                    val source = response.body.source()
                    val parser = SseParser()
                    while (true) {
                        val line = source.readUtf8Line() ?: return
                        val event = parser.feed(line) ?: continue
                        if (event.name != null && event.name != EVENT_NAME) continue
                        runCatching { gson.fromJson(event.data, LiveBusDto::class.java) }
                            .getOrNull()
                            ?.let { trySendBlocking(it) }
                    }
                }
            },
        )
        awaitClose { call.cancel() }
    }

    private companion object {
        const val PATH = "/api/bus/location/stream"
        const val EVENT_NAME = "location"
        const val READ_TIMEOUT_SECONDS = 45L
    }
}
