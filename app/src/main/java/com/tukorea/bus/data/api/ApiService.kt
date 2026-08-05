package com.tukorea.bus.data.api

import com.tukorea.bus.data.model.DirectionsResponse
import com.tukorea.bus.data.model.TempResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/temps")
    suspend fun getTemps(): TempResponse

    /**
     * 길찾기 API
     * @param start 출발지 좌표 (형식: "경도,위도" 예: "127.1054327,37.3595958")
     * @param end 도착지 좌표 (형식: "경도,위도" 예: "127.1080295,37.3612116")
     * @param waypoints 경유지 좌표 (형식: "경도1,위도1:경도2,위도2" 최대 5개)
     */
    @GET("/api/bus/directions/{start}/{end}")
    suspend fun getDirections(
        @Path("start") start: String,
        @Path("end") end: String,
        @Query("waypoints") waypoints: String? = null
    ): DirectionsResponse
}