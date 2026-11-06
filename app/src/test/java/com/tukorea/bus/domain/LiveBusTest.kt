package com.tukorea.bus.domain

import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.LiveBusStatus
import com.tukorea.bus.domain.model.ShuttleDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveBusTest {

    private fun bus(id: String, direction: ShuttleDirection? = null) =
        LiveBus(id, 37.34, 126.73, LiveBusStatus.RUNNING, 0L, direction)

    @Test
    fun `기사 앱 id에서 호차 번호를 읽는다`() {
        assertEquals(3, bus("bus-3").number)
        assertEquals(12, bus("bus-12").number)
        assertEquals(1, bus("bus-001").number)
        assertNull(bus("shuttle").number)
    }

    @Test
    fun `노선을 모르는 버스는 어느 노선에서든 보인다`() {
        assertTrue(bus("bus-1").isOn(ShuttleDirection.STATION_TO_CAMPUS1))
        assertTrue(bus("bus-1", ShuttleDirection.STATION_TO_CAMPUS1).isOn(ShuttleDirection.STATION_TO_CAMPUS1))
        assertFalse(bus("bus-1", ShuttleDirection.CAMPUS1_TO_STATION).isOn(ShuttleDirection.STATION_TO_CAMPUS1))
    }
}
