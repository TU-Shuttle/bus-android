package com.tukorea.bus.data.model

import com.google.gson.annotations.SerializedName

/**
 * 길찾기 API 응답 DTO
 * GET /api/bus/directions/{start}/{end}
 * 네이버 API 있는거 그대로 반환함
 */
data class DirectionsResponse(
    @SerializedName("data") val data: DirectionsData?,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("timestamp") val timestamp: String?
)

data class DirectionsData(
    @SerializedName("code") val code: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("currentDateTime") val currentDateTime: String?,
    @SerializedName("route") val route: RouteData?
)

data class RouteData(
    @SerializedName("trafast") val trafast: List<RouteOption>?,
    @SerializedName("traoptimal") val traoptimal: List<RouteOption>?,
    @SerializedName("tracomfort") val tracomfort: List<RouteOption>?
)

data class RouteOption(
    @SerializedName("summary") val summary: RouteSummary?,
    @SerializedName("path") val path: List<List<Double>>?,
    @SerializedName("section") val section: List<RouteSection>?,
    @SerializedName("guide") val guide: List<RouteGuide>?
)

data class RouteSummary(
    @SerializedName("start") val start: LocationInfo?,
    @SerializedName("goal") val goal: LocationInfo?,
    @SerializedName("distance") val distance: Int?,
    @SerializedName("duration") val duration: Int?,
    @SerializedName("departureTime") val departureTime: String?,
    @SerializedName("bbox") val bbox: List<List<Double>>?,
    @SerializedName("tollFare") val tollFare: Int?,
    @SerializedName("taxiFare") val taxiFare: Int?,
    @SerializedName("fuelPrice") val fuelPrice: Int?
)

data class LocationInfo(
    @SerializedName("location") val location: List<Double>?,
    @SerializedName("dir") val dir: Int?
)

data class RouteSection(
    @SerializedName("pointIndex") val pointIndex: Int?,
    @SerializedName("pointCount") val pointCount: Int?,
    @SerializedName("distance") val distance: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("congestion") val congestion: Int?,
    @SerializedName("speed") val speed: Int?
)

data class RouteGuide(
    @SerializedName("pointIndex") val pointIndex: Int?,
    @SerializedName("type") val type: Int?,
    @SerializedName("instructions") val instructions: String?,
    @SerializedName("distance") val distance: Int?,
    @SerializedName("duration") val duration: Int?
)
