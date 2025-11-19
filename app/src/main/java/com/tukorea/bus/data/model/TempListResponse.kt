package com.tukorea.bus.data.model

import com.tukorea.bus.domain.model.Temp

data class TempListResponse(
    val data: List<TempResponse>
) {
    fun toDomain(): List<Temp> = data.flatMap { it.toDomain() }
}