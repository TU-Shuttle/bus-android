package com.tukorea.bus.data.api

import com.tukorea.bus.data.model.TempListResponse
import retrofit2.http.GET

interface ApiService {
    @GET("/api/temps") // GET /temp
    suspend fun getTemps(): TempListResponse
}