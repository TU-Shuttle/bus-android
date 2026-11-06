package com.tukorea.bus.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tukorea.bus.data.model.ApiEnvelope
import com.tukorea.bus.data.model.ScheduleDto
import com.tukorea.bus.data.model.TimetableDto
import com.tukorea.bus.data.model.toDomain
import com.tukorea.bus.data.model.toMinuteOfDay
import com.tukorea.bus.domain.model.DayType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 서버(/api/bus/timetable) 응답 형태 그대로의 fixture로 변환을 검증한다. */
class MappersTest {

    private val gson = Gson()

    private val timetableJson = """
        {
          "data": {
            "title": "2학기 셔틀버스 시간표",
            "validFrom": "2026-09-01",
            "validTo": "2026-12-22",
            "fetchedAt": "2026-11-06T02:22:33.1",
            "schedules": [
              {"id": 1, "dayType": "WEEKDAY", "direction": "STATION_TO_CAMPUS1", "directionLabel": "정왕역 → 본교",
               "departTime": "08:40:00", "endTime": "10:00:00", "note": "수시운행"},
              {"id": 2, "dayType": "WEEKDAY", "direction": "CAMPUS2_TO_CAMPUS1", "departTime": "17:40:00",
               "endTime": null, "note": "오이도역 도착"},
              {"id": 3, "dayType": "WEEKDAY", "direction": "STATION_TO_CAMPUS1", "departTime": "17:00:00",
               "endTime": "22:17:00", "note": "★ 도착버스 탑승  ※ 17시 이후 시간은 파리바게뜨 건너편"},
              {"id": 4, "dayType": "HOLIDAY", "direction": "STATION_TO_CAMPUS1", "departTime": "09:00:00"},
              {"id": 5, "dayType": "WEEKDAY", "direction": "TO_THE_MOON", "departTime": "09:00:00"}
            ]
          },
          "message": "정보를 성공적으로 조회했습니다.",
          "status": 200
        }
    """.trimIndent()

    @Test
    fun `시간표를_도메인으로_바꾸고_모르는_코드는_버린다`() {
        val type = object : TypeToken<ApiEnvelope<TimetableDto>>() {}.type
        val envelope: ApiEnvelope<TimetableDto> = gson.fromJson(timetableJson, type)

        val timetable = envelope.data!!.toDomain()

        assertEquals("2학기 셔틀버스 시간표", timetable.title)
        assertEquals(3, timetable.departures.size)
        val window = timetable.departures[0]
        assertEquals(8 * 60 + 40, window.minute)
        assertEquals(10 * 60, window.endMinute)
        assertEquals(DayType.WEEKDAY, window.dayType)
        assertEquals("오이도역 도착", timetable.departures[1].note)
    }

    @Test
    fun `안내문의_강조기호와_겹친_공백을_정리한다`() {
        val type = object : TypeToken<ApiEnvelope<TimetableDto>>() {}.type
        val envelope: ApiEnvelope<TimetableDto> = gson.fromJson(timetableJson, type)

        assertEquals(
            "도착버스 탑승 17시 이후 시간은 파리바게뜨 건너편",
            envelope.data!!.toDomain().departures[2].note,
        )
    }

    @Test
    fun `시각_문자열을_분으로_바꾼다`() {
        assertEquals(580, "09:40:00".toMinuteOfDay())
        assertEquals(1337, "22:17".toMinuteOfDay())
        assertNull("25:00".toMinuteOfDay())
        assertNull("noon".toMinuteOfDay())
    }

    @Test
    fun `요일별_운행_요일을_ISO_번호로_바꾼다`() {
        val dto = ScheduleDto("WEEKDAY", "CAMPUS2_TO_OIDO_STATION", "17:40:00", null, null, listOf("WEDNESDAY", "monday", "NOPE"))

        val departure = dto.toDomain()!!

        assertEquals(com.tukorea.bus.domain.model.ShuttleDirection.CAMPUS2_TO_OIDO_STATION, departure.direction)
        assertEquals(setOf(1, 3), departure.daysOfWeek)
        assertEquals(null, ScheduleDto("WEEKDAY", "STATION_TO_CAMPUS1", "09:00", null, null).toDomain()!!.daysOfWeek)
    }
}
