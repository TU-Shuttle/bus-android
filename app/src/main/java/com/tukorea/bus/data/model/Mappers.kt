package com.tukorea.bus.data.model

import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.LiveBusStatus
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.domain.model.RideSummary
import com.tukorea.bus.domain.model.RideTarget
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.Timetable

/*
 * DTO → 도메인 변환. 앱이 모르는 코드(서버에 새 방향이 추가된 경우 등)나 깨진 행은 버린다.
 */

fun TimetableDto.toDomain(): Timetable =
    Timetable(
        title = title.orEmpty(),
        validFrom = validFrom,
        validTo = validTo,
        fetchedAt = fetchedAt,
        departures = schedules.orEmpty().mapNotNull { it.toDomain() },
    )

fun ScheduleDto.toDomain(): Departure? {
    val direction = enumOrNull<ShuttleDirection>(direction) ?: return null
    val dayType = enumOrNull<DayType>(dayType) ?: return null
    val minute = departTime?.toMinuteOfDay() ?: return null
    return Departure(
        direction = direction,
        dayType = dayType,
        minute = minute,
        endMinute = endTime?.toMinuteOfDay(),
        note = note?.cleanNote(),
    )
}

/** 원본 PDF의 강조 기호(★, ※)와 겹친 공백을 걷어 화면에 바로 쓸 수 있게 한다. */
private fun String.cleanNote(): String? =
    replace("★", " ").replace("※", " ").replace(Regex("\\s+"), " ").trim().takeIf { it.isNotEmpty() }

fun LiveBusDto.toDomain(): LiveBus? {
    val id = id ?: return null
    val lat = lat ?: return null
    val lng = lng ?: return null
    return LiveBus(
        id = id,
        latitude = lat,
        longitude = lng,
        status = enumOrNull<LiveBusStatus>(status) ?: LiveBusStatus.RUNNING,
        updatedAtMillis = updatedAt ?: 0L,
    )
}

fun RideDto.toDomain(): RideIntent? {
    return RideIntent(
        id = id ?: return null,
        serviceDate = serviceDate ?: return null,
        direction = enumOrNull<ShuttleDirection>(direction) ?: return null,
        departMinute = departTime?.toMinuteOfDay() ?: return null,
        dropOff = enumOrNull<ShuttleStop>(dropOff) ?: return null,
    )
}

fun RideSummaryDto.toDomain(): RideSummary? {
    return RideSummary(
        direction = enumOrNull<ShuttleDirection>(direction) ?: return null,
        departMinute = departTime?.toMinuteOfDay() ?: return null,
        riders = (riders ?: 0L).toInt(),
        dropOffs = dropOffs.orEmpty()
            .mapNotNull { (stop, count) -> enumOrNull<ShuttleStop>(stop)?.let { it to count.toInt() } }
            .toMap(),
    )
}

fun RideTarget.toBody(dropOff: ShuttleStop): CreateRideBody =
    CreateRideBody(
        serviceDate = serviceDate,
        direction = direction.name,
        departTime = "%02d:%02d".format(departMinute / 60, departMinute % 60),
        dropOff = dropOff.name,
    )

/** "09:40", "09:40:00" → 580 */
internal fun String.toMinuteOfDay(): Int? {
    val parts = split(":")
    if (parts.size < 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private inline fun <reified E : Enum<E>> enumOrNull(value: String?): E? =
    enumValues<E>().firstOrNull { it.name.equals(value, ignoreCase = true) }
