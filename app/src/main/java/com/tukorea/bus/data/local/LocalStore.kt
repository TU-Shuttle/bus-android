package com.tukorea.bus.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 기기에 남기는 값: 마지막으로 받은 시간표(오프라인용). */
@Singleton
class LocalStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val timetableJson: Flow<String?> = dataStore.data.map { it[TIMETABLE_JSON] }

    suspend fun saveTimetableJson(json: String) {
        dataStore.edit { it[TIMETABLE_JSON] = json }
    }

    private companion object {
        val TIMETABLE_JSON = stringPreferencesKey("timetable_json")
    }
}
