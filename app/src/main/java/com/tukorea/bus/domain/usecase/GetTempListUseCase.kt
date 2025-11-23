package com.tukorea.bus.domain.usecase

import com.tukorea.bus.domain.model.Temp
import com.tukorea.bus.domain.repository.TempRepository
import com.tukorea.bus.domain.util.AppResult
import javax.inject.Inject

class GetTempListUseCase @Inject constructor(
    private val repository: TempRepository
) {
    suspend operator fun invoke(): AppResult<List<Temp>> = repository.getTemps()
}