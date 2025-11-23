package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Temp
import com.tukorea.bus.domain.util.AppResult

interface TempRepository {
    suspend fun getTemps(): AppResult<List<Temp>>
}