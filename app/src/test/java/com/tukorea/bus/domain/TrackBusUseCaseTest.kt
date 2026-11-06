package com.tukorea.bus.domain

import com.tukorea.bus.domain.model.GeoPoint
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.LiveBusStatus
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleRoute
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.usecase.TrackBusUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackBusUseCaseTest {

    private val useCase = TrackBusUseCase()

    /** 북쪽으로 곧게 뻗은 약 1.1km 경로. */
    private val route = ShuttleRoute(
        ShuttleDirection.CAMPUS1_TO_STATION,
        listOf(GeoPoint(37.3400, 126.7300), GeoPoint(37.3500, 126.7300)),
    )

    private fun bus(lat: Double, atSeconds: Long, lng: Double = 126.7300) =
        LiveBus("bus1", lat, lng, LiveBusStatus.RUNNING, atSeconds * 1000)

    @Test
    fun `moving forward heads to destination with remaining distance and eta`() {
        // 10초에 약 111m → 40km/h
        val result = useCase(bus(37.3410, 10), bus(37.3400, 0), route)

        assertEquals(ShuttleStop.JEONGWANG_STATION, result.nextStop)
        assertEquals(40, result.speedKmh)
        assertEquals(0.0, result.headingDegrees!!, 1.0)
        assertEquals(1002.0, result.remainingMeters!!.toDouble(), 5.0)
        assertEquals(2, result.etaMinutes)
    }

    @Test
    fun `moving backward on the same road heads to origin`() {
        val result = useCase(bus(37.3480, 10), bus(37.3490, 0), route)

        assertEquals(ShuttleStop.CAMPUS1, result.nextStop)
        assertEquals(180.0, result.headingDegrees!!, 1.0)
        assertEquals(890.0, result.remainingMeters!!.toDouble(), 5.0)
    }

    @Test
    fun `off route bus has no next stop`() {
        val result = useCase(bus(37.3450, 10, lng = 126.7400), bus(37.3440, 0, lng = 126.7400), route)

        assertNull(result.nextStop)
        assertNull(result.etaMinutes)
        assertEquals(40, result.speedKmh)
    }

    @Test
    fun `first sighting assumes route direction and typical speed`() {
        val result = useCase(bus(37.3450, 0), previous = null, route = route)

        assertEquals(ShuttleStop.JEONGWANG_STATION, result.nextStop)
        assertNull(result.headingDegrees)
        assertNull(result.speedKmh)
        assertTrue(result.etaMinutes!! >= 1)
    }

    /** 정왕역 → 본교 서문 → 제2캠퍼스를 직선으로 잇는 경유 노선. */
    private val via = ShuttleRoute(
        ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
        listOf(ShuttleStop.JEONGWANG_STATION.point, ShuttleStop.CAMPUS1_WEST_GATE.point, ShuttleStop.CAMPUS2.point),
    )

    @Test
    fun `via route heads to the middle stop first and also gives the final stop`() {
        val start = ShuttleStop.JEONGWANG_STATION.point
        val result = useCase(
            LiveBus("bus-1", start.latitude, start.longitude, LiveBusStatus.RUNNING, 0, ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1),
            previous = null,
            route = via,
        )

        assertEquals(ShuttleStop.CAMPUS1_WEST_GATE, result.nextStop)
        assertEquals(ShuttleStop.CAMPUS2, result.finalStop)
        assertTrue(result.finalEtaMinutes!! > result.etaMinutes!!)
    }

    @Test
    fun `after the middle stop only the final stop remains`() {
        val gate = ShuttleStop.CAMPUS1_WEST_GATE.point
        val result = useCase(
            LiveBus("bus-1", gate.latitude, gate.longitude, LiveBusStatus.RUNNING, 0, ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1),
            previous = null,
            route = via,
        )

        assertEquals(ShuttleStop.CAMPUS2, result.nextStop)
        assertNull(result.finalStop)
    }

    @Test
    fun `bus on the same route is forward even if gps jitters backward`() {
        // 직전보다 조금 뒤로 튄 위치지만, 기사가 이 노선을 골랐으므로 정방향으로 본다.
        val now = LiveBus("bus-1", 37.3440, 126.7300, LiveBusStatus.RUNNING, 10_000, ShuttleDirection.CAMPUS1_TO_STATION)
        val before = LiveBus("bus-1", 37.3450, 126.7300, LiveBusStatus.RUNNING, 0, ShuttleDirection.CAMPUS1_TO_STATION)

        assertEquals(ShuttleStop.JEONGWANG_STATION, useCase(now, before, route).nextStop)
    }
}
