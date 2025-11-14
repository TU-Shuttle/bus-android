package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.Temp
import com.tukorea.bus.data.model.TempResponse
import com.tukorea.bus.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getTemps(): Resource<List<Temp>> = withContext(Dispatchers.IO) {
        try {
            val res = api.getTemps() // TempResponse
            Resource.Success(res.data) // res.data: List<Temp>
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}