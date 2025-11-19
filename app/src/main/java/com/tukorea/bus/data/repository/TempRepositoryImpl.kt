package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.TempResponse
import com.tukorea.bus.domain.model.Temp
import com.tukorea.bus.domain.repository.TempRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRepositoryImpl @Inject constructor(
    private val api: ApiService
) : TempRepository {
    override suspend fun getTemps(): List<Temp> {
        val response: TempResponse = api.getTemps()
        return response.data
    }
}