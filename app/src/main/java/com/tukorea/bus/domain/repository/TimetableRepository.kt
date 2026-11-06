package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.Timetable
import com.tukorea.bus.domain.util.AppResult
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {
    /** 마지막으로 받은 시간표(오프라인이면 저장해 둔 값). 한 번도 못 받았으면 null. */
    val timetable: Flow<Timetable?>

    /** 서버에서 새로 받아 [timetable]을 갱신한다. 실패해도 기존 값은 유지된다. */
    suspend fun refresh(): AppResult<Unit>
}
