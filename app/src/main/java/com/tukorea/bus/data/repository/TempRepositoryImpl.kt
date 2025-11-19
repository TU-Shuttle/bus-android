package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.TempListResponse
import com.tukorea.bus.domain.model.Temp
import com.tukorea.bus.domain.repository.TempRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRepositoryImpl @Inject constructor(
    private val api: ApiService
) : TempRepository {
    override suspend fun getTemps(): List<Temp> {
        val response: TempListResponse = api.getTemps()
        return response.toDomain()
    }
}