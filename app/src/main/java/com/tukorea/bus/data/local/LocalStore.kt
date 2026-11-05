package com.tukorea.bus.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** 기기에 남기는 값: 마지막 시간표(오프라인용)와 익명 기기 id. */
@Singleton
class LocalStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val timetableJson: Flow<String?> = dataStore.data.map { it[TIMETABLE_JSON] }

    suspend fun saveTimetableJson(json: String) {
        dataStore.edit { it[TIMETABLE_JSON] = json }
    }

    /** 로그인 없이 내 탑승 예정을 구분하기 위한 무작위 id. 처음 쓸 때 만든다. */
    suspend fun deviceId(): String {
        dataStore.data.first()[DEVICE_ID]?.let { return it }
        val created = UUID.randomUUID().toString()
        dataStore.edit { prefs -> if (prefs[DEVICE_ID] == null) prefs[DEVICE_ID] = created }
        return dataStore.data.first()[DEVICE_ID] ?: created
    }

    private companion object {
        val TIMETABLE_JSON = stringPreferencesKey("timetable_json")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }
}
