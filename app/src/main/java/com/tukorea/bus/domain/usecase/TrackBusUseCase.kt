package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.BusTracking
import com.tukorea.bus.domain.model.GeoPoint
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.ShuttleRoute
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.bearingDegrees
import com.tukorea.bus.domain.model.distanceMeters
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * 직전 위치와 지금 위치로 버스의 방향·속도를 구하고, 고른 노선 경로 위라면 다음 정류장까지 남은 거리와 시간을 낸다.
 * 같은 도로를 반대로 달리는 버스도 있으므로, 경로 위에서 앞으로 갔는지 뒤로 갔는지 보고 다음 정류장을 정한다.
 */
class TrackBusUseCase @Inject constructor() {

    operator fun invoke(current: LiveBus, previous: LiveBus?, route: ShuttleRoute?): BusTracking {
        val now = current.point
        val before = previous?.point
        val moved = before?.let { distanceMeters(it, now) } ?: 0.0
        val seconds = previous?.let { (current.updatedAtMillis - it.updatedAtMillis) / 1000.0 } ?: 0.0

        val heading = if (before != null && moved >= MIN_MOVE_METERS) bearingDegrees(before, now) else null
        val speedMps = if (seconds > 0) moved / seconds else null

        val here = route?.project(now)?.takeIf { it.offMeters <= ON_ROUTE_METERS }
        if (route == null || here == null) {
            return BusTracking(current, heading, speedMps?.toKmh(), nextStop = null, remainingMeters = null, etaMinutes = null)
        }
        val there = before?.let(route::project)
        // 기사가 같은 노선을 골랐으면 그대로 정방향이다. 모르면 직전 위치와 비교하고, 그것도 모르면(첫 위치, 정차 중) 정방향으로 본다.
        val forward = when {
            current.direction == route.direction -> true
            there == null -> true
            else -> here.alongMeters >= there.alongMeters - BACKWARD_TOLERANCE_METERS
        }
        // 진행 방향 기준 "출발점부터 온 거리"로 맞춘 뒤, 아직 지나지 않은 첫 정류장을 다음 정류장으로 본다.
        val travelled = if (forward) here.alongMeters else route.lengthMeters - here.alongMeters
        val stopsAhead = (if (forward) route.direction.stops else route.direction.stops.reversed())
            .mapNotNull { stop ->
                val along = route.project(stop.point)?.alongMeters ?: return@mapNotNull null
                stop to (if (forward) along else route.lengthMeters - along)
            }
            .filter { (_, at) -> at - travelled > ARRIVED_METERS }
        val travelSpeed = speedMps?.takeIf { it >= MIN_SPEED_FOR_ETA_MPS } ?: TYPICAL_SPEED_MPS
        fun minutes(meters: Double) = ceil(meters / travelSpeed / 60.0).toInt()

        val next = stopsAhead.firstOrNull()
            // 마지막 정류장 바로 앞이면 그 정류장에 곧 도착한다.
            ?: ((if (forward) route.direction.destination else route.direction.origin) to travelled)
        val nextRemaining = (next.second - travelled).coerceAtLeast(0.0)
        val final = stopsAhead.lastOrNull()?.takeIf { stopsAhead.size > 1 }
        return BusTracking(
            bus = current,
            headingDegrees = heading,
            speedKmh = speedMps?.toKmh(),
            nextStop = next.first,
            remainingMeters = nextRemaining.roundToInt(),
            etaMinutes = minutes(nextRemaining),
            finalStop = final?.first,
            finalEtaMinutes = final?.let { minutes(it.second - travelled) },
        )
    }

    private val LiveBus.point: GeoPoint get() = GeoPoint(latitude, longitude)

    private fun Double.toKmh(): Int = (this * 3.6).roundToInt()

    private companion object {
        const val MIN_MOVE_METERS = 3.0
        const val ON_ROUTE_METERS = 80.0
        const val BACKWARD_TOLERANCE_METERS = 5.0
        const val MIN_SPEED_FOR_ETA_MPS = 3.0

        /** 정류장을 이만큼 안쪽까지 지나면 다음 정류장으로 넘어간다. */
        const val ARRIVED_METERS = 40.0

        /** 시내 셔틀 평균 속도(약 25km/h). 정차 중이면 이 값으로 남은 시간을 어림한다. */
        const val TYPICAL_SPEED_MPS = 7.0
    }
}
