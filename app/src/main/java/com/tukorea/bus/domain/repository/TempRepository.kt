package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Temp

interface TempRepository {
    suspend fun getTemps(): List<Temp>
}