package com.example.financetracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class RecordFilter {
    ALL,
    INCOME,
    EXPENSE
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val RECORD_FILTER = stringPreferencesKey("record_filter")
    }

    val recordFilter: Flow<RecordFilter> = dataStore.data
        .catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val savedValue = preferences[RECORD_FILTER]
            RecordFilter.entries.firstOrNull { it.name == savedValue } ?: RecordFilter.ALL
        }

    suspend fun saveRecordFilter(filter: RecordFilter) {
        dataStore.edit { preferences ->
            preferences[RECORD_FILTER] = filter.name
        }
    }
}
