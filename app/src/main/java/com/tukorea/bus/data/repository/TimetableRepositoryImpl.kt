package com.tukorea.bus.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.local.LocalStore
import com.tukorea.bus.data.model.TimetableDto
import com.tukorea.bus.data.model.toDomain
import com.tukorea.bus.data.util.NetworkErrorHandler
import com.tukorea.bus.domain.error.AppError
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.repository.TimetableRepository
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 서버 시간표를 받아 기기에 저장한다. 화면은 항상 저장된 값을 보므로 오프라인에서도 시간표가 보인다. */
@Singleton
class TimetableRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val localStore: LocalStore,
    private val gson: Gson,
) : TimetableRepository {

    override val timetable: Flow<Timetable?> =
        localStore.timetableJson.map { json -> json?.let(::decode)?.toDomain() }

    override suspend fun refresh(): AppResult<Unit> {
        val result = NetworkErrorHandler.handleNetworkRequest { api.getTimetable().data }
        return when (result) {
            is Result.Error -> result
            is Result.Success -> {
                val dto = result.data ?: return Result.Error(AppError.NotFound)
                localStore.saveTimetableJson(gson.toJson(dto))
                Result.Success(Unit)
            }
        }
    }

    private fun decode(json: String): TimetableDto? =
        try {
            gson.fromJson(json, TimetableDto::class.java)
        } catch (e: JsonSyntaxException) {
            null
        }
}
