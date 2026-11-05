package com.tukorea.bus.data.api

import com.tukorea.bus.data.model.ApiEnvelope
import com.tukorea.bus.data.model.CreateRideBody
import com.tukorea.bus.data.model.LiveBusDto
import com.tukorea.bus.data.model.RideDto
import com.tukorea.bus.data.model.RideSummaryDto
import com.tukorea.bus.data.model.TimetableDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/bus/timetable")
    suspend fun getTimetable(): ApiEnvelope<TimetableDto>

    @GET("api/bus/locations")
    suspend fun getLiveBuses(): List<LiveBusDto>

    @GET("api/rides/mine")
    suspend fun getMyRides(@Header(DEVICE_HEADER) deviceId: String): ApiEnvelope<List<RideDto>>

    @POST("api/rides")
    suspend fun reserveRide(
        @Header(DEVICE_HEADER) deviceId: String,
        @Body body: CreateRideBody,
    ): ApiEnvelope<RideDto>

    @DELETE("api/rides/{id}")
    suspend fun cancelRide(
        @Header(DEVICE_HEADER) deviceId: String,
        @Path("id") rideId: Long,
    ): ApiEnvelope<Any>

    @GET("api/rides/summary")
    suspend fun getRideSummary(@Query("date") serviceDate: String): ApiEnvelope<List<RideSummaryDto>>

    private companion object {
        const val DEVICE_HEADER = "X-Device-Id"
    }
}
