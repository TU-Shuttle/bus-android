package com.tukorea.bus.data.remote

/**
 * Server-Sent Events 한 줄씩을 받아 이벤트 단위로 묶는다(서버: Spring SseEmitter).
 * `event:`/`data:` 줄을 모으다 빈 줄에서 이벤트를 내보낸다. `:`로 시작하는 줄(keep-alive 주석)은 무시한다.
 */
class SseParser {
    data class Event(val name: String?, val data: String)

    private var name: String? = null
    private val data = StringBuilder()

    /** 이벤트가 완성되면 돌려준다. */
    fun feed(line: String): Event? {
        when {
            line.isEmpty() -> return flush()
            line.startsWith(":") -> Unit
            line.startsWith("event:") -> name = line.removePrefix("event:").trim()
            line.startsWith("data:") -> {
                if (data.isNotEmpty()) data.append('\n')
                data.append(line.removePrefix("data:").removePrefix(" "))
            }
        }
        return null
    }

    private fun flush(): Event? {
        val event = if (data.isEmpty()) null else Event(name, data.toString())
        name = null
        data.clear()
        return event
    }
}
