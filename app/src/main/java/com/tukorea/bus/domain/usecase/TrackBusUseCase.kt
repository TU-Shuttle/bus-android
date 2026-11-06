package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.BusTracking
import com.tukorea.bus.domain.model.GeoPoint
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.ShuttleRoute
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
        // 앞으로 가는지 모르겠으면(첫 위치, 정차 중) 노선 방향으로 간다고 본다.
        val forward = there == null || here.alongMeters >= there.alongMeters - BACKWARD_TOLERANCE_METERS
        val remaining = if (forward) route.lengthMeters - here.alongMeters else here.alongMeters
        val travelSpeed = speedMps?.takeIf { it >= MIN_SPEED_FOR_ETA_MPS } ?: TYPICAL_SPEED_MPS
        return BusTracking(
            bus = current,
            headingDegrees = heading,
            speedKmh = speedMps?.toKmh(),
            nextStop = if (forward) route.direction.destination else route.direction.origin,
            remainingMeters = remaining.roundToInt(),
            etaMinutes = ceil(remaining / travelSpeed / 60.0).toInt(),
        )
    }

    private val LiveBus.point: GeoPoint get() = GeoPoint(latitude, longitude)

    private fun Double.toKmh(): Int = (this * 3.6).roundToInt()

    private companion object {
        const val MIN_MOVE_METERS = 3.0
        const val ON_ROUTE_METERS = 80.0
        const val BACKWARD_TOLERANCE_METERS = 5.0
        const val MIN_SPEED_FOR_ETA_MPS = 3.0

        /** 시내 셔틀 평균 속도(약 25km/h). 정차 중이면 이 값으로 남은 시간을 어림한다. */
        const val TYPICAL_SPEED_MPS = 7.0
    }
}
