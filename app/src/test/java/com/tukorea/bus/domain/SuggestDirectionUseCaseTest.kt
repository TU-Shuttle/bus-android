package com.tukorea.bus.domain

import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.usecase.SuggestDirectionUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestDirectionUseCaseTest {

    private val useCase = SuggestDirectionUseCase()

    private val timetable = Timetable(
        title = "",
        validFrom = null,
        validTo = null,
        fetchedAt = null,
        departures = listOf(
            Departure(ShuttleDirection.STATION_TO_CAMPUS1, DayType.WEEKDAY, 600),
            Departure(ShuttleDirection.CAMPUS1_TO_STATION, DayType.WEEKDAY, 900),
            Departure(ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1, DayType.SATURDAY, 540),
            Departure(ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1, DayType.SATURDAY, 990),
        ),
    )

    @Test
    fun `평일_오전엔_등교_오후엔_하교_방향`() {
        assertEquals(
            ShuttleDirection.STATION_TO_CAMPUS1,
            useCase(timetable, ServiceMoment("2026-11-06", 5, 8 * 60)),
        )
        assertEquals(
            ShuttleDirection.CAMPUS1_TO_STATION,
            useCase(timetable, ServiceMoment("2026-11-06", 5, 15 * 60)),
        )
    }

    @Test
    fun `토요일은_그날_운행하는_경유_노선에서_고른다`() {
        assertEquals(
            ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
            useCase(timetable, ServiceMoment("2026-11-07", 6, 8 * 60)),
        )
        assertEquals(
            ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1,
            useCase(timetable, ServiceMoment("2026-11-07", 6, 15 * 60)),
        )
    }

    @Test
    fun `일요일엔_고를_방향이_없다`() {
        assertNull(useCase(timetable, ServiceMoment("2026-11-08", 7, 600)))
    }
}
