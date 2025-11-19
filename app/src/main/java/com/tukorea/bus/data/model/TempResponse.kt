package com.tukorea.bus.data.model

import com.tukorea.bus.domain.model.Temp

data class TempResponse(
    val data: List<Temp>
) {
    fun toDomain(): List<Temp> = data.map { Temp(it.temp) }
}