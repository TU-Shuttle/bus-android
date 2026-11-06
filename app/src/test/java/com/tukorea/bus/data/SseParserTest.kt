package com.tukorea.bus.data

import com.tukorea.bus.data.remote.SseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SseParserTest {

    private fun parse(vararg lines: String): List<SseParser.Event> {
        val parser = SseParser()
        return lines.mapNotNull(parser::feed)
    }

    @Test
    fun `Spring SseEmitter 형식의 이벤트를 읽는다`() {
        val events = parse("event:location", "data:{\"id\":\"bus-1\"}", "")
        assertEquals(listOf(SseParser.Event("location", "{\"id\":\"bus-1\"}")), events)
    }

    @Test
    fun `keep-alive 주석과 빈 이벤트는 건너뛴다`() {
        assertEquals(emptyList<SseParser.Event>(), parse(":keep-alive", "", ""))
    }

    @Test
    fun `여러 이벤트와 여러 줄 data를 나눠 읽는다`() {
        val events = parse("event:location", "data: a", "data: b", "", "data:c", "")
        assertEquals(SseParser.Event("location", "a\nb"), events[0])
        assertEquals(SseParser.Event(null, "c"), events[1])
    }

    @Test
    fun `빈 줄 전에는 이벤트를 내지 않는다`() {
        assertNull(SseParser().feed("data:x"))
    }
}
