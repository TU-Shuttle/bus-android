package com.tukorea.bus.domain

import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.model.UpcomingDepartures
import com.tukorea.bus.domain.usecase.GetUpcomingDeparturesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetUpcomingDeparturesUseCaseTest {

    private val useCase = GetUpcomingDeparturesUseCase()
    private val direction = ShuttleDirection.STATION_TO_CAMPUS1

    private val timetable = Timetable(
        title = "2학기",
        validFrom = "2026-09-01",
        validTo = "2026-12-22",
        fetchedAt = null,
        departures = listOf(
            Departure(direction, DayType.WEEKDAY, minute = 8 * 60 + 40, endMinute = 10 * 60, note = "수시운행"),
            Departure(direction, DayType.WEEKDAY, minute = 10 * 60),
            Departure(direction, DayType.WEEKDAY, minute = 10 * 60 + 10),
            Departure(direction, DayType.WEEKDAY, minute = 10 * 60 + 15),
            Departure(direction, DayType.WEEKDAY, minute = 10 * 60 + 20),
            Departure(ShuttleDirection.CAMPUS1_TO_STATION, DayType.WEEKDAY, minute = 9 * 60),
        ),
    )

    private fun weekdayAt(minute: Int) = ServiceMoment("2026-11-06", isoDayOfWeek = 5, minuteOfDay = minute)

    @Test
    fun `수시운행_구간이면_종료시각과_이후_정시출발을_함께_준다`() {
        val result = useCase(timetable, direction, weekdayAt(9 * 60)) as UpcomingDepartures.Scheduled

        assertEquals(10 * 60, result.onDemandUntil)
        assertEquals(listOf(600, 610, 615), result.next.map { it.minute })
    }

    @Test
    fun `정각에_출발하는_버스도_다음_셔틀로_본다`() {
        val result = useCase(timetable, direction, weekdayAt(10 * 60 + 10)) as UpcomingDepartures.Scheduled

        assertNull(result.onDemandUntil)
        assertEquals(10 * 60 + 10, result.next.first().minute)
    }

    @Test
    fun `마지막_출발이_지나면_운행_종료와_첫차를_알려준다`() {
        val result = useCase(timetable, direction, weekdayAt(23 * 60))

        assertEquals(UpcomingDepartures.Finished(timetable.departures.first()), result)
    }

    @Test
    fun `일요일이나_그날_없는_방향은_운행_없음`() {
        val sunday = ServiceMoment("2026-11-08", isoDayOfWeek = 7, minuteOfDay = 600)
        val saturday = ServiceMoment("2026-11-07", isoDayOfWeek = 6, minuteOfDay = 600)

        assertEquals(UpcomingDepartures.NoService, useCase(timetable, direction, sunday))
        assertEquals(UpcomingDepartures.NoService, useCase(timetable, direction, saturday))
    }

    @Test
    fun `수시운행_시작_전이면_정시출발이_없어도_종료가_아니다`() {
        val onlyWindow = timetable.copy(departures = timetable.departures.take(1))

        val result = useCase(onlyWindow, direction, weekdayAt(8 * 60))

        assertEquals(UpcomingDepartures.Scheduled(onDemandUntil = null, next = emptyList()), result)
    }

    @Test
    fun `시간표_적용기간_밖인지_판단한다`() {
        assertEquals(true, timetable.isValidOn("2026-11-06"))
        assertEquals(false, timetable.isValidOn("2026-12-23"))
        assertEquals(false, timetable.isValidOn("2026-08-31"))
    }
}
