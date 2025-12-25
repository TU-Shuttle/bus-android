package com.tukorea.bus.data.repository

import com.tukorea.bus.data.api.ApiService
import com.tukorea.bus.data.model.DirectionsResponse
import com.tukorea.bus.data.model.RouteOption
import com.tukorea.bus.data.util.NetworkErrorHandler
import com.tukorea.bus.domain.model.CongestionLevel
import com.tukorea.bus.domain.model.Coordinate
import com.tukorea.bus.domain.model.Directions
import com.tukorea.bus.domain.model.GuideInfo
import com.tukorea.bus.domain.model.Route
import com.tukorea.bus.domain.model.RouteSummaryInfo
import com.tukorea.bus.domain.model.RouteType
import com.tukorea.bus.domain.model.SectionInfo
import com.tukorea.bus.domain.repository.DirectionsRepository
import com.tukorea.bus.domain.util.AppResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 길찾기 Repository 구현체
 */
@Singleton
class DirectionsRepositoryImpl @Inject constructor(
    private val api: ApiService
) : DirectionsRepository {

    override suspend fun getDirections(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        waypoints: List<Coordinate>
    ): AppResult<Directions> {
        return NetworkErrorHandler.handleNetworkRequest {
            // API 좌표 형식: "경도,위도"
            val start = "$startLng,$startLat"
            val end = "$endLng,$endLat"
            
            // 경유지 형식: "경도1,위도1:경도2,위도2" (최대 5개)
            val waypointsParam = if (waypoints.isNotEmpty()) {
                waypoints.take(5).joinToString(":") { "${it.longitude},${it.latitude}" }
            } else null
            
            val response = api.getDirections(start, end, waypointsParam)
            response.toDomain()
        }
    }

    private fun DirectionsResponse.toDomain(): Directions {
        val routes = mutableListOf<Route>()
        
        data?.route?.let { routeData ->
            routeData.traoptimal?.forEach { option ->
                routes.add(option.toRoute(RouteType.TRAOPTIMAL))
            }
            routeData.trafast?.forEach { option ->
                routes.add(option.toRoute(RouteType.TRAFAST))
            }
            routeData.tracomfort?.forEach { option ->
                routes.add(option.toRoute(RouteType.TRACOMFORT))
            }
        }

        return Directions(
            message = data?.message ?: message ?: "",
            routes = routes
        )
    }

    private fun RouteOption.toRoute(type: RouteType): Route {
        return Route(
            type = type,
            summary = RouteSummaryInfo(
                startLocation = Coordinate(
                    latitude = summary?.start?.location?.getOrNull(1) ?: 0.0,
                    longitude = summary?.start?.location?.getOrNull(0) ?: 0.0
                ),
                goalLocation = Coordinate(
                    latitude = summary?.goal?.location?.getOrNull(1) ?: 0.0,
                    longitude = summary?.goal?.location?.getOrNull(0) ?: 0.0
                ),
                totalDistanceMeters = summary?.distance ?: 0,
                totalDurationMinutes = (summary?.duration ?: 0) / 60000,
                departureTime = summary?.departureTime ?: "",
                tollFare = summary?.tollFare ?: 0,
                taxiFare = summary?.taxiFare ?: 0,
                fuelPrice = summary?.fuelPrice ?: 0
            ),
            path = path?.map { coords ->
                Coordinate(
                    latitude = coords.getOrNull(1) ?: 0.0,
                    longitude = coords.getOrNull(0) ?: 0.0
                )
            } ?: emptyList(),
            sections = section?.map { sec ->
                SectionInfo(
                    name = sec.name ?: "",
                    distance = sec.distance ?: 0,
                    congestion = when (sec.congestion) {
                        0 -> CongestionLevel.UNKNOWN
                        1 -> CongestionLevel.SMOOTH
                        2 -> CongestionLevel.SLOW
                        3 -> CongestionLevel.DELAY
                        4 -> CongestionLevel.JAM
                        else -> CongestionLevel.UNKNOWN
                    },
                    speed = sec.speed ?: 0
                )
            } ?: emptyList(),
            guides = guide?.map { g ->
                GuideInfo(
                    instructions = g.instructions ?: "",
                    distance = g.distance ?: 0,
                    durationMinutes = (g.duration ?: 0) / 60000
                )
            } ?: emptyList()
        )
    }
}
