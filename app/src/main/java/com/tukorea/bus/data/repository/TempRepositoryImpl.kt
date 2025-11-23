package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.TempResponse
import com.tukorea.bus.data.util.NetworkErrorHandler
import com.tukorea.bus.domain.model.Temp
import com.tukorea.bus.domain.repository.TempRepository
import com.tukorea.bus.domain.util.AppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRepositoryImpl @Inject constructor(
    private val api: ApiService
) : TempRepository {
    override suspend fun getTemps(): AppResult<List<Temp>> {
        return NetworkErrorHandler.handleNetworkRequest {
            val response: TempResponse = api.getTemps()
            response.data
        }
    }
}