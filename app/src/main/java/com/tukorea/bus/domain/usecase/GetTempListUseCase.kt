package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Temp
import com.tukorea.bus.domain.repository.TempRepository
import javax.inject.Inject

class GetTempListUseCase @Inject constructor(
    private val repository: TempRepository
) {
    suspend operator fun invoke(): List<Temp> = repository.getTemps()
}