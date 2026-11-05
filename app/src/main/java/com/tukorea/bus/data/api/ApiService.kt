package com.tukorea.bus.data.api

import com.tukorea.bus.data.model.ApiEnvelope
import com.tukorea.bus.data.model.LiveBusDto
import com.tukorea.bus.data.model.TimetableDto
import retrofit2.http.GET

interface ApiService {

    @GET("api/bus/timetable")
    suspend fun getTimetable(): ApiEnvelope<TimetableDto>

    @GET("api/bus/locations")
    suspend fun getLiveBuses(): List<LiveBusDto>
}
