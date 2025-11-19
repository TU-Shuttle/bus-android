package com.tukorea.bus.data.model

import com.tukorea.bus.domain.model.Temp

data class TempResponse(
    val data: List<Temp> = emptyList()
)